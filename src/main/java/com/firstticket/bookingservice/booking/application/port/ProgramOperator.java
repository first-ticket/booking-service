package com.firstticket.bookingservice.booking.application.port;

import com.firstticket.bookingservice.booking.application.port.dto.ProgramScheduleResult;
import java.util.UUID;

public interface ProgramOperator {
    ProgramScheduleResult validateSchedule(UUID scheduleId);
}
