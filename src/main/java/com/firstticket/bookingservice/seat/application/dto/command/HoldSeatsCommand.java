package com.firstticket.bookingservice.seat.application.dto.command;

import java.util.List;
import java.util.UUID;

public record HoldSeatsCommand(
    List<UUID> seatIds,
    UUID scheduleId
) {}
