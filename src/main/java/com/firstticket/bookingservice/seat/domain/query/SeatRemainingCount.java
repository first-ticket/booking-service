package com.firstticket.bookingservice.seat.domain.query;

import java.util.UUID;

public record SeatRemainingCount(
    UUID scheduleId,
    long remainingCount
) {}
