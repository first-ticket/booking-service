package com.firstticket.bookingservice.booking.presentation.dto.request;

import com.firstticket.bookingservice.booking.application.dto.command.CreateBookingCommand;
import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(
    UUID programId,
    UUID scheduleId,
    List<UUID> seatList
) {
    public CreateBookingCommand toCommand() {

        return new CreateBookingCommand(
            this.programId,
            this.scheduleId,
            this.seatList
        );
    }
}
