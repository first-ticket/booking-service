package com.firstticket.bookingservice.seat.infrastructure.redis;

import com.firstticket.bookingservice.global.token.BookingTokenProvider;
import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
    "spring.kafka.consumer.group-id=test-group"
})
class RedissonSeatHoldManagerTest {

    @MockitoBean
    private BookingTokenProvider bookingTokenProvider;

    @Container
    static RedisContainer redis = new RedisContainer(
        RedisContainer.DEFAULT_IMAGE_NAME.withTag("7.2")
    );

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private RedissonSeatHoldManager seatHoldManager;

    private final UUID scheduleId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final String sessionId = UUID.randomUUID().toString();

    @AfterEach
    void tearDown() {
        List<SeatId> heldSeatIds = seatHoldManager.getHeldSeatIds(sessionId);
        seatHoldManager.releaseAll(heldSeatIds, scheduleId, userId, sessionId);
    }

    @Test
    @DisplayName("좌석 선점 성공")
    void hold_success() {
        List<SeatId> seatIds = List.of(SeatId.of(), SeatId.of());

        seatHoldManager.hold(seatIds, scheduleId, userId, sessionId);

        assertThat(seatHoldManager.isHeld(seatIds, userId, sessionId)).isTrue();
    }

    @Test
    @DisplayName("이미 선점된 좌석 선점 시도 시 예외 발생")
    void hold_alreadyHeld() {
        SeatId seatId = SeatId.of();
        List<SeatId> seatIds = List.of(seatId);

        seatHoldManager.hold(seatIds, scheduleId, userId, sessionId);

        assertThatThrownBy(() ->
            seatHoldManager.hold(seatIds, scheduleId, UUID.randomUUID(), UUID.randomUUID().toString())
        )
            .isInstanceOf(SeatException.class)
            .satisfies(e -> assertThat(((SeatException) e).getErrorCode())
                .isEqualTo(SeatErrorCode.SEAT_ALREADY_HELD));
    }

    @Test
    @DisplayName("선점 실패 시 성공한 좌석 롤백")
    void hold_rollback() {
        SeatId alreadyHeldSeat = SeatId.of();
        SeatId newSeat = SeatId.of();

        // alreadyHeldSeat 선점
        seatHoldManager.hold(List.of(alreadyHeldSeat), scheduleId, UUID.randomUUID(), UUID.randomUUID().toString());

        // newSeat + alreadyHeldSeat 동시 선점 시도 시 실패
        assertThatThrownBy(() ->
            seatHoldManager.hold(List.of(newSeat, alreadyHeldSeat), scheduleId, userId, sessionId)
        ).isInstanceOf(SeatException.class);

        // newSeat 롤백 확인
        assertThat(seatHoldManager.isHeld(List.of(newSeat), userId, sessionId)).isFalse();
    }

    @Test
    @DisplayName("선점 해제 성공")
    void releaseAll_success() {
        List<SeatId> seatIds = List.of(SeatId.of(), SeatId.of());

        seatHoldManager.hold(seatIds, scheduleId, userId, sessionId);
        seatHoldManager.releaseAll(seatIds, scheduleId, userId, sessionId);

        assertThat(seatHoldManager.isHeld(seatIds, userId, sessionId)).isFalse();
    }

    @Test
    @DisplayName("세션별 선점 목록 조회 성공")
    void getHeldSeatIds_success() {
        List<SeatId> seatIds = List.of(SeatId.of(), SeatId.of());

        seatHoldManager.hold(seatIds, scheduleId, userId, sessionId);

        List<SeatId> heldSeatIds = seatHoldManager.getHeldSeatIds(sessionId);
        assertThat(heldSeatIds).containsExactlyInAnyOrderElementsOf(seatIds);
    }

    @Test
    @DisplayName("다른 사용자의 선점 좌석 검증 실패")
    void isHeld_wrongUser() {
        List<SeatId> seatIds = List.of(SeatId.of());

        seatHoldManager.hold(seatIds, scheduleId, userId, sessionId);

        assertThat(seatHoldManager.isHeld(seatIds, UUID.randomUUID(), UUID.randomUUID().toString())).isFalse();
    }

    @Test
    @DisplayName("같은 유저가 동일 스케줄에 새로 선점 시도 시 기존 선점 해제")
    void hold_releasePreviousHold() {
        SeatId oldSeat = SeatId.of();
        SeatId newSeat = SeatId.of();
        String newSessionId = UUID.randomUUID().toString();

        seatHoldManager.hold(List.of(oldSeat), scheduleId, userId, sessionId);
        seatHoldManager.hold(List.of(newSeat), scheduleId, userId, newSessionId);

        // 기존 선점 해제 확인
        assertThat(seatHoldManager.isHeld(List.of(oldSeat), userId, sessionId)).isFalse();
        // 새 선점 확인
        assertThat(seatHoldManager.isHeld(List.of(newSeat), userId, newSessionId)).isTrue();
    }

    @Test
    @DisplayName("동시에 같은 좌석 선점 시도 시 하나만 성공")
    void hold_concurrency() throws InterruptedException {
        SeatId seatId = SeatId.of();
        List<SeatId> seatIds = List.of(seatId);

        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String sessionId1 = UUID.randomUUID().toString();
        String sessionId2 = UUID.randomUUID().toString();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        CountDownLatch latch = new CountDownLatch(1);

        Thread t1 = new Thread(() -> {
            try {
                latch.await();
                seatHoldManager.hold(seatIds, scheduleId, userId1, sessionId1);
                successCount.incrementAndGet();
            } catch (SeatException e) {
                if (e.getErrorCode() == SeatErrorCode.SEAT_HOLD_FAILED) {
                    failCount.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                latch.await();
                seatHoldManager.hold(seatIds, scheduleId, userId2, sessionId2);
                successCount.incrementAndGet();
            } catch (SeatException e) {
                if (e.getErrorCode() == SeatErrorCode.SEAT_HOLD_FAILED) {
                    failCount.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        t1.start();
        t2.start();
        latch.countDown();
        t1.join();
        t2.join();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);
    }
}
