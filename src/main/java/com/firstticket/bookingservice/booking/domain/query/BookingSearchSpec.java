package com.firstticket.bookingservice.booking.domain.query;

import com.firstticket.bookingservice.booking.domain.BookingStatus;
import java.time.LocalDate;
import java.util.UUID;

// 검색 조건 레코드
public record BookingSearchSpec(
    UUID userId,
    BookingStatus status,
    LocalDate startDate,
    LocalDate endDate
) {
}
