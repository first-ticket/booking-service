package com.firstticket.bookingservice.booking.infrastructure.messaging.payload;

import java.util.UUID;

public record BookingPaymentRefundPayload(
    UUID paymentId,
    UUID bookingId,
    UUID userId,
    String reason
) {
    public static BookingPaymentRefundPayload of(UUID paymentId, UUID userId, UUID bookingId, String reason){
        return new BookingPaymentRefundPayload(paymentId,userId,bookingId,reason);
    }
}
