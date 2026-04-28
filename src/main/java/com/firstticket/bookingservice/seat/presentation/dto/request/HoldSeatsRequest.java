package com.firstticket.bookingservice.seat.presentation.dto.request;

import com.firstticket.bookingservice.seat.application.dto.command.HoldSeatsCommand;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record HoldSeatsRequest(
    @NotEmpty
    List<UUID> seatIds
) {
    public HoldSeatsCommand toCommand(UUID scheduleId) {
        return new HoldSeatsCommand(seatIds, scheduleId);
    }
}
