package com.firstticket.bookingservice.seat.domain.service;

import com.firstticket.bookingservice.seat.domain.SeatId;

import java.util.List;
import java.util.UUID;

public interface SeatHoldManager {

    void hold(List<SeatId> seatIds, UUID scheduleId, UUID userId, String sessionId);

    boolean releaseAll(List<SeatId> seatIds, UUID scheduleId, UUID userId, String sessionId);

    boolean isHeld(List<SeatId> seatIds, UUID userId, String sessionId);

    List<SeatId> getHeldSeatIds(String sessionId);
}
