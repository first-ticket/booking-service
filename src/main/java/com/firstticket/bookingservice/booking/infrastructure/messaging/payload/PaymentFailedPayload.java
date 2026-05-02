package com.firstticket.bookingservice.booking.infrastructure.messaging.payload;

import java.util.UUID;

public record PaymentFailedPayload(
    UUID paymentId,
    UUID bookingId,
    UUID userId,
    String reason
) {
}
