package com.firstticket.bookingservice.seat.presentation;

import com.firstticket.bookingservice.seat.application.SeatCommandService;
import com.firstticket.bookingservice.seat.presentation.dto.request.SeatIdsRequest;
import com.firstticket.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/seats")
public class SeatInternalController {

    private final SeatCommandService seatCommandService;

    @PostMapping("/hold/valid")
    public ResponseEntity<ApiResponse<Void>> validateHold(
        @Valid @RequestBody SeatIdsRequest request,
        @RequestHeader("X-User-Id") UUID userId,
        @RequestHeader("X-Session-Id") String sessionId
    ) {
        seatCommandService.validateHold(request.seatIds(), userId, sessionId);
        return ApiResponse.success(SeatSuccessCode.SEAT_HOLD_VALID);
    }
}
