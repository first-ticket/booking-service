package com.firstticket.bookingservice.seat.presentation;

import com.firstticket.bookingservice.seat.application.SeatCommandService;
import com.firstticket.bookingservice.seat.presentation.dto.request.HoldSeatsRequest;
import com.firstticket.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seats")
public class SeatController {

    private final SeatCommandService seatCommandService;

    @PostMapping("/schedules/{scheduleId}/hold")
    public ResponseEntity<ApiResponse<Void>> holdSeats(
        @PathVariable UUID scheduleId,
        @RequestBody HoldSeatsRequest request,
        @RequestHeader("X-User-Id") UUID userId,
        @RequestHeader("X-Session-Id") String sessionId
    ) {
        seatCommandService.holdSeats(request.toCommand(scheduleId), userId, sessionId);
        return ApiResponse.success(SeatSuccessCode.SEAT_HELD);
    }
}
