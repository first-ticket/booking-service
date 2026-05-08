package com.firstticket.bookingservice.booking.infrastructure.messaging.payload;

import java.util.UUID;

public record BookingExpiredPayload(
    UUID paymentId,
    UUID bookingId,
    UUID userId,
    String reason
) {
    public static BookingExpiredPayload of(UUID paymentId, UUID bookingId, UUID userId, String reason){
        return new BookingExpiredPayload(paymentId,bookingId,userId,reason);
    }
}
