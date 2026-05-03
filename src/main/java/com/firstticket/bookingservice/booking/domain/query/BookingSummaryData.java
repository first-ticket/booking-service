package com.firstticket.bookingservice.booking.domain.query;


import com.firstticket.bookingservice.booking.domain.BookingStatus;
import java.time.LocalDateTime;
import java.util.UUID;

// DB 조회 결과 레코드
public record BookingSummaryData(
    UUID bookingId,
    String programTitle,
    BookingStatus status,
    Long totalPrice,
    int totalCount,
    LocalDateTime updatedAt
) {
}
