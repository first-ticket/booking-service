package com.firstticket.bookingservice.booking.domain.service.dto;

import java.util.UUID;

public record PaymentResult(
    UUID paymentId,
    String orderId,
    long amount
) {
}
