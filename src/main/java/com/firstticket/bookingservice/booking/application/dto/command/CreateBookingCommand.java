package com.firstticket.bookingservice.booking.application.dto.command;

import java.util.List;
import java.util.UUID;

public record CreateBookingCommand(
    UUID programId,
    UUID scheduleId,
    List<UUID> seatList
) {}
