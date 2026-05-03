package com.firstticket.bookingservice.booking.infrastructure.client.program_service;

import com.firstticket.bookingservice.booking.application.port.ProgramOperator;
import com.firstticket.bookingservice.booking.application.port.dto.ProgramScheduleResult;
import com.firstticket.bookingservice.booking.infrastructure.client.program_service.dto.ProgramScheduleResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProgramOperatorImpl implements ProgramOperator {

    private final ProgramClient programClient;

    @Override
    public ProgramScheduleResult validateSchedule(UUID scheduleId) {
        ProgramScheduleResponse response = programClient.validateSchedule(scheduleId);
        return new ProgramScheduleResult(
            response.programTitle(),
            response.venueName(),
            response.venueAddress(),
            response.eventStartAt(),
            response.eventEndAt(),
            response.saleStartAt(),
            response.saleEndAt()
        );
    }
}
