package com.firstticket.bookingservice.booking.infrastructure.messaging.payload;

import java.util.UUID;

public record PaymentCancelRequestedPayload(
    UUID paymentId,
    UUID userId,
    UUID bookingId,
    String reason
) {
    public static PaymentCancelRequestedPayload of(UUID paymentId, UUID userId, UUID bookingId, String reason){
        return new PaymentCancelRequestedPayload(paymentId, userId, bookingId, reason);
    }
}
