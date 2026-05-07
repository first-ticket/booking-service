package com.firstticket.bookingservice.booking.presentation.dto.response;

import com.firstticket.bookingservice.booking.application.dto.result.BookingResult;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingCreateResponse(
    UUID bookingId,
    UUID paymentId,
    String orderId,
    String programTitle,
    LocalDateTime eventStartAt,
    LocalDateTime eventEndAt,
    long totalPrice,
    int totalCount
) {
    public static BookingCreateResponse from(BookingResult bookingResult){
        return new BookingCreateResponse(
            bookingResult.bookingId(),
            bookingResult.paymentId(),
            bookingResult.orderId(),
            bookingResult.programTitle(),
            bookingResult.eventStartAt(),
            bookingResult.eventEndAt(),
            bookingResult.totalPrice(),
            bookingResult.totalCount()
        );
    }
}
