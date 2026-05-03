package com.firstticket.bookingservice.booking.presentation.dto.request;

import com.firstticket.bookingservice.booking.application.dto.command.CreateBookingCommand;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(
    @NotNull UUID programId,
    @NotNull UUID scheduleId,
    @NotEmpty List<UUID> seatList
) {
    public CreateBookingCommand toCommand() {

        return new CreateBookingCommand(
            this.programId,
            this.scheduleId,
            this.seatList
        );
    }
}
