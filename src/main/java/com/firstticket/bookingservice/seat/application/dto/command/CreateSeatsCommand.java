package com.firstticket.bookingservice.seat.application.dto.command;

import com.firstticket.bookingservice.seat.domain.SeatType;

import java.util.List;
import java.util.UUID;

public record CreateSeatsCommand(
    UUID programId,
    UUID scheduleId,
    List<SeatTemplateCommand> seatTemplates

) {
    public record SeatTemplateCommand(
        UUID sectionId,
        String sectionName,
        SeatType seatType,
        Integer rowCount,
        Integer colCount,
        Integer capacity,
        int price

    ) {}
}
