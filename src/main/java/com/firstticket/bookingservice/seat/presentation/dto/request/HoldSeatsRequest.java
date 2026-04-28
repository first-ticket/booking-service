package com.firstticket.bookingservice.seat.presentation.dto.request;

import com.firstticket.bookingservice.seat.application.dto.command.HoldSeatsCommand;

import java.util.List;
import java.util.UUID;

public record HoldSeatsRequest(
    List<UUID> seatIds
) {
    public HoldSeatsCommand toCommand(UUID scheduleId) {
        return new HoldSeatsCommand(seatIds, scheduleId);
    }
}
