package com.firstticket.bookingservice.seat.infrastructure.redis;

import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import com.firstticket.bookingservice.seat.domain.service.SeatHoldManager;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedissonSeatHoldManager implements SeatHoldManager {

    private static final long HOLD_TTL_MINUTES = 10; // 좌석 선점 유지 시간: 10분
    private static final long LOCK_WAIT_SECONDS = 0; // 락 획득 대기 시간: 0초 -> 락 획득 실패 시 즉시 실패
    private static final long LOCK_LEASE_SECONDS = 1; // 락 유지 시간: 1초

    private final RedissonClient redissonClient;

    /**
     * 여러 좌석을 한번에 선점
     * - 좌석마다 개별 분산락을 획등하여 동시 선점을 방지
     * - 하나라도 실패하면 이미 선점된 좌석을 모두 롤백 후 선점 실패 예외 발행
     */
    @Override
    public void hold(UUID scheduleId, List<SeatId> seatIds, UUID userId, String sessionId) {
        List<SeatId> heldSeatIds = new ArrayList<>();
        try {

            for (SeatId seatId : seatIds) {
                String lockKey = lockKey(seatId);
                String holdKey = holdKey(seatId);

                RLock lock = redissonClient.getLock(lockKey);
                try {

                    // 분산락 획득 시도: 동시에 같은 좌석을 접근하는 요청을 직렬화
                    if (!lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS)) {
                        throw new SeatException(SeatErrorCode.SEAT_HOLD_FAILED);
                    }

                    // Redis에서 선점 여부 확인
                    // 선점하려는 좌석으로 생성된 holdKey의 TTL이 살아있다면 선점 중
                    RBucket<String> holdBucket = redissonClient.getBucket(holdKey);
                    if (holdBucket.isExists()) {
                        throw new SeatException(SeatErrorCode.SEAT_ALREADY_HELD);
                    }

                    // 선점 등록: held:{seatId} = userId:sessionId (TTL 10분)
                    holdBucket.set(userId + ":" + sessionId, Duration.ofMinutes(HOLD_TTL_MINUTES));
                    heldSeatIds.add(seatId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SeatException(SeatErrorCode.SEAT_HOLD_FAILED);
                } finally {
                    // 락은 반드시 해제 (성공/실패 모두)
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }

            // 세션별 선점 목록 등록: session:{sessionId}:{scheduleId} = [seatIds] (TTL 10분)
            // 세션 만료 또는 예매 취소 시 선점 좌석 일괄 해제에 사용
            String sessionKey = sessionKey(sessionId, scheduleId);
            RList<String> sessionList = redissonClient.getList(sessionKey);
            seatIds.forEach(seatId -> sessionList.add(seatId.id().toString()));
            sessionList.expire(Duration.ofMinutes(HOLD_TTL_MINUTES));
        } catch (SeatException e) {
            // 좌석 하나라도 선점 실패 시 모두 선점 해제 (롤백)
            heldSeatIds.forEach(this::release);
            throw e;
        }
    }

    /**
     * 세션에서 선점 중인 좌석 모두 해제
     * holdKey를 삭제하여 각 좌석을 선점 가능 상태로 돌리고 sessionKey도 함께 삭제
     */
    @Override
    public void releaseAll(UUID scheduleId, List<SeatId> seatIds, String sessionId) {
        seatIds.forEach(this::release);
        redissonClient.getBucket(sessionKey(sessionId, scheduleId)).delete();
    }

    /**
     * 선점 중인 좌석이 요청한 사용자의 것인지 확인
     * held:{seatId}에 저장된 userId:sessionId와 요청한 userId:sessionId와 일치하는지 검증
     */
    @Override
    public boolean isHeld(List<SeatId> seatIds, UUID userId, String sessionId) {
        for (SeatId seatId : seatIds) {
            RBucket<String> bucket = redissonClient.getBucket(holdKey(seatId));
            String value = bucket.get();
            if (value == null || !value.equals(userId + ":" + sessionId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 세션에서 선점중인 좌석 ID 목록 조회
     * session:{sessionId}:{scheduleId} 키에서 seatId 목록을 반환
     */
    @Override
    public List<SeatId> getHeldSeatIds(String sessionId, UUID scheduleId) {
        RList<String> sessionList = redissonClient.getList(sessionKey(sessionId, scheduleId));
        return sessionList.stream()
            .map(id -> SeatId.of(UUID.fromString(id)))
            .toList();
    }

    // 내부용 단건 선점 해제
    private void release(SeatId seatId) {
        redissonClient.getBucket(holdKey(seatId)).delete();
    }

    // lock:{seatId} - 분산락 키
    private String lockKey(SeatId seatId) {
        return "lock:" + seatId.id();
    }

    // held:{seatId} - 선점 상태 저장 키
    private String holdKey(SeatId seatId) {
        return "held:" + seatId.id();
    }

    // session:{sessionId}:{scheduleId} - 세션별 선점 목록 키
    private String sessionKey(String sessionId, UUID scheduleId) {
        return "session:" + sessionId + ":" + scheduleId;
    }

}
