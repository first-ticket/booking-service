package com.firstticket.bookingservice.seat.infrastructure.redis;

import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import com.firstticket.bookingservice.seat.domain.service.SeatHoldManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonSeatHoldManager implements SeatHoldManager {

    private static final long HOLD_TTL_MINUTES = 10; // 좌석 선점 유지 시간: 10분
    private static final long HOLD_LOCK_WAIT_SECONDS = 0; // 선점 락 획득 대기 시간: 0초 -> 락 획득 실패 시 즉시 실패
    private static final long RELEASE_LOCK_WAIT_SECONDS = 3; // 선점 해제 락 대기 시간: 3초

    private final RedissonClient redissonClient;

    /**
     * 여러 좌석을 한번에 선점
     * - 기존 선점이 있으면 먼저 해제 후 새로 선점
     * - 좌석마다 개별 분산락을 획득하여 동시 선점을 방지
     * - 하나라도 실패하면 이미 선점된 좌석을 모두 롤백 후 선점 실패 예외 발행
     */
    @Override
    public void hold(List<SeatId> seatIds, UUID scheduleId, UUID userId, String sessionId) {
        RLock userLock = redissonClient.getLock("lock:user-hold:" + userId + ":" + scheduleId);
        boolean userLocked = false;
        try {
            userLocked = userLock.tryLock(HOLD_LOCK_WAIT_SECONDS, -1, TimeUnit.SECONDS);
            if (!userLocked) {
                throw new SeatException(SeatErrorCode.SEAT_HOLD_FAILED);
            }

            // 기존 선점이 있으면 해제
            RBucket<String> userHoldBucket = redissonClient.getBucket(userHoldKey(userId, scheduleId));
            String existingSessionId = userHoldBucket.get();
            if (existingSessionId != null) {
                List<SeatId> existingSeatIds = getHeldSeatIds(existingSessionId);
                releaseAll(existingSeatIds, scheduleId, userId, existingSessionId);
            }

            List<SeatId> heldSeatIds = new ArrayList<>();
            RList<String> sessionList = redissonClient.getList(sessionKey(sessionId));
            try {
                for (SeatId seatId : seatIds) {
                    RLock lock = redissonClient.getLock(lockKey(seatId));
                    try {
                        // 분산락 획득 시도: 즉시 실패, leaseTime=-1로 watchdog 활성화 (락 자동 갱신)
                        if (!lock.tryLock(HOLD_LOCK_WAIT_SECONDS, -1, TimeUnit.SECONDS)) {
                            throw new SeatException(SeatErrorCode.SEAT_HOLD_FAILED);
                        }

                        // 선점 여부 확인
                        RBucket<String> holdBucket = redissonClient.getBucket(holdKey(seatId));
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
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                }

                // 세션별 선점 목록 등록: session:{sessionId} = [seatIds] (TTL 10분)
                seatIds.forEach(seatId -> sessionList.add(seatId.id().toString()));
                sessionList.expire(Duration.ofMinutes(HOLD_TTL_MINUTES));

                // 유저 활성 선점 등록: user-hold:{userId}:{scheduleId} = sessionId (TTL 10분)
                userHoldBucket.set(sessionId, Duration.ofMinutes(HOLD_TTL_MINUTES));

            } catch (RuntimeException e) {
                heldSeatIds.forEach(this::release);
                sessionList.delete();
                throw e;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SeatException(SeatErrorCode.SEAT_HOLD_FAILED);
        } finally {
            if (userLocked && userLock.isHeldByCurrentThread()) {
                userLock.unlock();
            }
        }
    }

    /**
     * 세션에서 선점 중인 좌석 모두 해제
     * holdKey를 삭제하여 각 좌석을 선점 가능 상태로 돌리고 sessionKey, userHoldKey도 함께 삭제
     */
    @Override
    public void releaseAll(List<SeatId> seatIds, UUID scheduleId, UUID userId, String sessionId) {
        boolean allReleased = true;
        for (SeatId seatId : seatIds) {
            RLock lock = redissonClient.getLock(lockKey(seatId));
            boolean locked = false;
            try {
                // 분산락 획득 시도: 3초 대기
                locked = lock.tryLock(RELEASE_LOCK_WAIT_SECONDS, -1, TimeUnit.SECONDS);
                if (!locked) {
                    allReleased = false;
                    continue;
                }
                RBucket<String> holdBucket = redissonClient.getBucket(holdKey(seatId));
                String value = holdBucket.get();
                if ((userId + ":" + sessionId).equals(value)) {
                    holdBucket.delete();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("좌석 선점 해제 중 인터럽트 발생. seatId: {}", seatId.id());
                allReleased = false;
            } finally {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        if (!allReleased) {
            log.warn("일부 좌석 선점 해제 실패. sessionId: {}", sessionId);
            return;
        }

        redissonClient.getList(sessionKey(sessionId)).delete();
        RBucket<String> userHoldBucket = redissonClient.getBucket(userHoldKey(userId, scheduleId));
        if (sessionId.equals(userHoldBucket.get())) {
            userHoldBucket.delete();
        }
    }

    /**
     * 선점 중인 좌석이 요청한 사용자의 것인지 확인
     * held:{seatId}에 저장된 userId:sessionId와 요청한 userId:sessionId가 일치하는지 검증
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
     * 세션에서 선점 중인 좌석 ID 목록 조회
     * session:{sessionId} 키에서 seatId 목록을 반환
     */
    @Override
    public List<SeatId> getHeldSeatIds(String sessionId) {
        RList<String> sessionList = redissonClient.getList(sessionKey(sessionId));
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

    // session:{sessionId} - 세션별 선점 목록 키
    private String sessionKey(String sessionId) {
        return "session:" + sessionId;
    }

    // user-hold:{userId}:{scheduleId} - 유저 활성 선점 키
    private String userHoldKey(UUID userId, UUID scheduleId) {
        return "user-hold:" + userId + ":" + scheduleId;
    }
}
