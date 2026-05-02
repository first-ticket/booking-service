package com.firstticket.bookingservice.booking.infrastructure.client.program_service;

import com.firstticket.bookingservice.booking.infrastructure.client.program_service.dto.ProgramScheduleResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "program-service", path = "/internal/v1/programs", configuration = ProgramClientConfig.class)
public interface ProgramClient {

    @GetMapping("/{scheduleId}/info")
    ProgramScheduleResponse validateSchedule(@PathVariable UUID scheduleId);
}
