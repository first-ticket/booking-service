package com.firstticket.bookingservice.seat.domain.service;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeatManager {

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

    public void reserveSeats(List<Seat> seats, UUID scheduleId, UUID userId, String sessionId) {
        if (!validateHold(seats.stream().map(Seat::getId).toList(), userId, sessionId)) {
            throw new SeatException(SeatErrorCode.SEAT_NOT_HELD);
        }
        seats.forEach(Seat::reserve);
        releaseSeats(seats.stream().map(Seat::getId).toList(), scheduleId, userId, sessionId);
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
