package com.firstticket.bookingservice.seat.domain.service;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private final SeatHoldManager seatHoldManager;

    public void holdSeats(List<Seat> seats, UUID scheduleId, UUID userId, String sessionId) {
        for (Seat seat : seats) {
            if (!seat.isAvailable()) {
                throw new SeatException(SeatErrorCode.SEAT_NOT_AVAILABLE);
            }
        }

        List<SeatId> seatIds = seats.stream().map(Seat::getId).toList();

        seatHoldManager.hold(seatIds, scheduleId, userId, sessionId);
    }

    public boolean releaseSeats(List<SeatId> seatIds, UUID scheduleId, UUID userId, String sessionId) {
        return seatHoldManager.releaseAll(seatIds, scheduleId, userId, sessionId);
    }

    public boolean validateHold(List<SeatId> seatIds, UUID userId, String sessionId) {
        return seatHoldManager.isHeld(seatIds, userId, sessionId);
    }

    public List<SeatId> getHeldSeats(String sessionId) {
        return seatHoldManager.getHeldSeatIds(sessionId);
    }

}
