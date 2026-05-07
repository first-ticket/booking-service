package com.firstticket.bookingservice.booking.application.dto.result;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResult(
    UUID bookingId,
    UUID paymentId,
    String orderId,
    String programTitle,
    LocalDateTime eventStartAt,
    LocalDateTime eventEndAt,
    long totalPrice,
    int totalCount
) {
}
