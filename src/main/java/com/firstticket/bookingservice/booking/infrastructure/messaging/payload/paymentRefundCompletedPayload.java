package com.firstticket.bookingservice.booking.infrastructure.messaging.payload;

import java.util.UUID;

public record paymentRefundCompletedPayload(
    UUID paymentId,
    UUID bookingId,
    UUID userId
) {
}
