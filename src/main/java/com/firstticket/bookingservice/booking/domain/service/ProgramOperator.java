package com.firstticket.bookingservice.booking.domain.service;

import com.firstticket.bookingservice.booking.domain.service.dto.ProgramScheduleResult;
import java.util.UUID;

public interface ProgramOperator {
    ProgramScheduleResult validateSchedule(UUID scheduleId);
}
