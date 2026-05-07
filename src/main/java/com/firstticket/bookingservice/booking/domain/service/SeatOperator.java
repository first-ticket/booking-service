package com.firstticket.bookingservice.booking.domain.service;

import com.firstticket.bookingservice.booking.domain.service.dto.HeldSeatResult;
import java.util.List;
import java.util.UUID;

public interface SeatOperator {
    void validateHold(List<UUID> seatIds, UUID userId, String sessionId);
    void reserveSeat(List<UUID> seatIds, UUID scheduleId, UUID userId, String sessionId);
    List<HeldSeatResult> getHeldSeats(UUID scheduleId, String sessionId);
}
