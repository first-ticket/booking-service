package com.firstticket.bookingservice.booking.infrastructure.messaging.payload;

import java.util.UUID;

public record PaymentCompletedPayload(
    UUID paymentId,
    UUID bookingId,
    long amount,
    String status
) {
}
