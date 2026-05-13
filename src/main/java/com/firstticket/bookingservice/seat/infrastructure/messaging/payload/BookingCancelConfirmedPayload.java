package com.firstticket.bookingservice.seat.infrastructure.messaging.payload;

import java.util.List;
import java.util.UUID;

public record BookingCancelConfirmedPayload(
    UUID scheduleId,
    List<UUID> seatList
) {}
