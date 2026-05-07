package com.firstticket.bookingservice.booking.infrastructure.messaging.payload;

import java.util.UUID;

public record RefundCompletedPayload(
    UUID paymentId,
    UUID bookingId,
    UUID userId
) {
}
