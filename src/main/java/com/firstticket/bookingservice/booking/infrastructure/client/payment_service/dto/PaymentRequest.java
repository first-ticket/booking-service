package com.firstticket.bookingservice.booking.infrastructure.client.payment_service.dto;

import java.util.UUID;

public record PaymentRequest(
    UUID bookingId,
    UUID userId,
    long finalAmount
) {
}
