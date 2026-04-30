package com.firstticket.bookingservice.seat.presentation.dto.response;

import com.firstticket.bookingservice.seat.application.dto.result.SeatRemainingResult;

import java.util.UUID;

public record SeatRemainingResponse(
    UUID scheduleId,
    int remainingCount
) {
    public static SeatRemainingResponse from(SeatRemainingResult result) {
        return new SeatRemainingResponse(result.scheduleId(), result.remainingCount());
    }
}
