package com.firstticket.bookingservice.booking.infrastructure.client.program_service.dto;

import com.firstticket.bookingservice.booking.domain.service.dto.ProgramScheduleResult;
import java.time.LocalDateTime;

public record ProgramScheduleResponse(
    String programTitle,
    String venueName,
    String venueAddress,
    LocalDateTime eventStartAt,
    LocalDateTime eventEndAt,
    LocalDateTime saleStartAt,
    LocalDateTime saleEndAt
) {
    public ProgramScheduleResult toResult(){
        return new ProgramScheduleResult(
            programTitle,
            venueName,
            venueAddress,
            eventStartAt,
            eventEndAt,
            saleStartAt,
            saleEndAt
        );
    }
}
