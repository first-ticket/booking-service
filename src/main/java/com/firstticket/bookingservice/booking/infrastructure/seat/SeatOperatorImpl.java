package com.firstticket.bookingservice.booking.infrastructure.seat;

import com.firstticket.bookingservice.booking.domain.service.SeatOperator;
import com.firstticket.bookingservice.booking.domain.service.vo.HeldSeatResult;
import com.firstticket.bookingservice.seat.infrastructure.internal.SeatConfirm;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatOperatorImpl implements SeatOperator {

    private final SeatConfirm seatConfirm;

    @Override
    public void validateHold(List<UUID> seatIds, UUID userId, String sessionId) {
            seatConfirm.validateHold(seatIds, userId, sessionId);
    }

    @Override
    public void reserveSeat(List<UUID> seatIds, UUID scheduleId, UUID userId, String sessionId) {
            seatConfirm.reserveSeat(seatIds, scheduleId, userId, sessionId);
    }

    @Override
    public List<HeldSeatResult> getHeldSeats(UUID scheduleId, String sessionId) {
        return seatConfirm.getHeldSeats(scheduleId, sessionId).stream()
            .map(s -> new HeldSeatResult(s.seatId(),s.seatInfo(), (long) s.price()))
            .toList();
    }
}
