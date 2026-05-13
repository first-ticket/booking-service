package com.firstticket.bookingservice.seat.infrastructure.messaging.payload;

import com.firstticket.bookingservice.seat.application.dto.command.CreateSeatsCommand;
import com.firstticket.bookingservice.seat.domain.SeatType;

import java.util.List;
import java.util.UUID;

public record ScheduleCreatedPayload(
    UUID programId,
    UUID scheduleId,
    List<SeatTemplatePayload> seatTemplates
) {

    public CreateSeatsCommand toCommand() {
        return new CreateSeatsCommand(
            programId,
            scheduleId,
            seatTemplates.stream()
                .map(template -> new CreateSeatsCommand.SeatTemplateCommand(
                    template.sectionId,
                    template.sectionName,
                    template.seatType,
                    template.rowCount,
                    template.colCount,
                    template.capacity,
                    template.price
                ))
                .toList()
        );
    }

    public record SeatTemplatePayload(
        UUID sectionId,
        String sectionName,
        SeatType seatType,
        Integer rowCount,
        Integer colCount,
        Integer capacity,
        int price
    ) {}
}
