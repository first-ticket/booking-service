package com.firstticket.bookingservice.booking.domain.service.vo;

import java.time.LocalDateTime;

public record ProgramScheduleResult(
    String programTitle,
    String venueName,
    String venueAddress,
    LocalDateTime eventStartAt,
    LocalDateTime eventEndAt,
    LocalDateTime saleStartAt,
    LocalDateTime saleEndAt
) {
}
