package com.firstticket.bookingservice.booking.infrastructure.client.payment_service.dto;

import java.util.UUID;

public record PaymentResponse(
    UUID paymentId,
    String orderId,
    long amount
) {
}
