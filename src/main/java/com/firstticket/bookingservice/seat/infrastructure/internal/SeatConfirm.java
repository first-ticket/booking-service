package com.firstticket.bookingservice.seat.infrastructure.internal;

import com.firstticket.bookingservice.seat.application.SeatCommandService;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 예매 애그리거트에서 좌석 확정 및 선점 유효성 확인을 위해 호출하는 내부 진입점입니다.
 */
@Component
@RequiredArgsConstructor
public class SeatConfirm {

    private final SeatCommandService seatCommandService;

    public void validateHold(List<UUID> seatIds, UUID userId, String sessionId) {
        seatCommandService.validateHold(seatIds, userId, sessionId);
    }

    public void reserveSeat(List<UUID> seatIds, UUID scheduleId, UUID userId, String sessionId) {
        seatCommandService.reserveSeats(seatIds, scheduleId, userId, sessionId);
    }
}
