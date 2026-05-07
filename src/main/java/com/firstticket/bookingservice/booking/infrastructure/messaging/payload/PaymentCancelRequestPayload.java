package com.firstticket.bookingservice.booking.infrastructure.messaging.payload;

import java.util.UUID;

public record PaymentCancelRequestPayload(
    UUID paymentId,
    UUID userId,
    UUID bookingId,
    String reason
) {
    public static PaymentCancelRequestPayload of(UUID paymentId, UUID userId, UUID bookingId, String reason){
        return new PaymentCancelRequestPayload(paymentId, userId, bookingId, reason);
    }
}
