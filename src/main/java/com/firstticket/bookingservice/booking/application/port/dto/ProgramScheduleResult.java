package com.firstticket.bookingservice.booking.application.port.dto;

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
