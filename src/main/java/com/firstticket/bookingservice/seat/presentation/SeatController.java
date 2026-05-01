package com.firstticket.bookingservice.seat.presentation;

import com.firstticket.bookingservice.global.annotation.BookingToken;
import com.firstticket.bookingservice.global.token.BookingTokenClaims;
import com.firstticket.bookingservice.seat.application.SeatCommandService;
import com.firstticket.bookingservice.seat.application.SeatQueryService;
import com.firstticket.bookingservice.seat.presentation.dto.request.SeatIdsRequest;
import com.firstticket.bookingservice.seat.presentation.dto.response.HeldSeatItemResponse;
import com.firstticket.bookingservice.seat.presentation.dto.response.SeatResponse;
import com.firstticket.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seats")
public class SeatController {

    private final SeatCommandService seatCommandService;
    private final SeatQueryService seatQueryService;

    @GetMapping("/schedules/{scheduleId}")
    public ResponseEntity<ApiResponse<SeatResponse>> getSeatList(
        @PathVariable UUID scheduleId
    ) {
        return ApiResponse.success(SeatSuccessCode.SEAT_LIST_OK,
            SeatResponse.from(seatQueryService.getSeatList(scheduleId)));
    }

    @GetMapping("/schedules/{scheduleId}/hold")
    public ResponseEntity<ApiResponse<List<HeldSeatItemResponse>>> getHeldSeatList(
        @PathVariable UUID scheduleId,
        @BookingToken BookingTokenClaims claims,
        @RequestHeader("Booking-Session-Token") String sessionToken
    ) {
        String token = sessionToken.startsWith("Bearer ") ? sessionToken.substring(7).trim() : sessionToken;
        return ApiResponse.success(SeatSuccessCode.SEAT_HELD_LIST_OK,
            seatQueryService.getHeldSeats(scheduleId, token).stream()
                .map(HeldSeatItemResponse::from)
                .toList());
    }

    @PostMapping("/schedules/{scheduleId}/hold")
    public ResponseEntity<ApiResponse<Void>> holdSeats(
        @PathVariable UUID scheduleId,
        @Valid @RequestBody SeatIdsRequest request,
        @RequestHeader("X-User-Id") UUID userId,
        @RequestHeader("X-Session-Id") String sessionId
    ) {
        seatCommandService.holdSeats(request.seatIds(), scheduleId, userId, sessionId);
        return ApiResponse.success(SeatSuccessCode.SEAT_HELD);
    }

    @DeleteMapping("/schedules/{scheduleId}/hold")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(
        @PathVariable UUID scheduleId,
        @RequestHeader("X-User-Id") UUID userId,
        @RequestHeader("X-Session-Id") String sessionId
    ) {
        seatCommandService.releaseSeats(scheduleId, userId, sessionId);
        return ApiResponse.success(SeatSuccessCode.SEAT_RELEASED);
    }
}
