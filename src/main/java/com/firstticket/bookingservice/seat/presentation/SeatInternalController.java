package com.firstticket.bookingservice.seat.presentation;

import com.firstticket.bookingservice.seat.application.SeatQueryService;
import com.firstticket.bookingservice.seat.application.dto.result.SeatRemainingResult;
import com.firstticket.bookingservice.seat.presentation.dto.response.SeatRemainingResponse;
import com.firstticket.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/seats")
public class SeatInternalController {

    private final SeatQueryService seatQueryService;

    @GetMapping("/remaining/{programId}")
    public ResponseEntity<ApiResponse<List<SeatRemainingResponse>>> getRemainingCounts(
        @PathVariable UUID programId
    ) {
        return ApiResponse.success(SeatSuccessCode.SEAT_REMAINING_OK,
            seatQueryService.getRemainingCounts(programId).stream()
                .map(SeatRemainingResponse::from)
                .toList());
    }
}
