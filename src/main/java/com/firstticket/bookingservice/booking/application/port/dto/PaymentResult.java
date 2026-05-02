package com.firstticket.bookingservice.booking.application.port.dto;

import java.util.UUID;

public record PaymentResult(
    UUID paymentId,
    String orderId,
    long amount
) {
}
