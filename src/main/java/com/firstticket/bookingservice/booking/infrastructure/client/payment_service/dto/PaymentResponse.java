package com.firstticket.bookingservice.booking.infrastructure.client.payment_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
    UUID paymentId,
    UUID userId,
    String orderId,
    long amount,
    String status,
    LocalDateTime requestedAt,
    LocalDateTime approvedAt
) {
}
