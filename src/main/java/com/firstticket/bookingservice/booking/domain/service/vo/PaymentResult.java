package com.firstticket.bookingservice.booking.domain.service.vo;

import java.util.UUID;

public record PaymentResult(
    UUID paymentId,
    String orderId,
    long amount
) {
}
