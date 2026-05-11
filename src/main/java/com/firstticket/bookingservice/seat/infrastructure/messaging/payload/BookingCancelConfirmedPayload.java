package com.firstticket.bookingservice.seat.infrastructure.messaging.payload;

import java.util.List;
import java.util.UUID;

public record BookingCancelConfirmedPayload(
    List<UUID> seatList
) {}
