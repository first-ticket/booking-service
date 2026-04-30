package com.firstticket.bookingservice.seat.application.dto.result;

import java.util.UUID;

public record SeatRemainingResult(
    UUID scheduleId,
    int remainingCount
) {}
