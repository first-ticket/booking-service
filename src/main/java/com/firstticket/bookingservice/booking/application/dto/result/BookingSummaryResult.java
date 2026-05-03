package com.firstticket.bookingservice.booking.application.dto.result;

import com.firstticket.bookingservice.booking.domain.BookingStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingSummaryResult(
    UUID bookingId,
    String programTitle,
    BookingStatus status,
    Long totalPrice,
    int totalCount,
    LocalDateTime updatedAt
) {
    public static BookingSummaryResult of(
        UUID bookingId,
        String programTitle,
        BookingStatus status,
        Long totalPrice,
        int totalCount,
        LocalDateTime updatedAt
    ){

            return new BookingSummaryResult(
                bookingId,
                programTitle,
                status,
                totalPrice,
                totalCount,
                updatedAt
        );
    }
}
