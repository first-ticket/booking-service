package com.firstticket.bookingservice.seat.domain.event;

import java.util.UUID;

public record SeatCacheRefreshEvent(UUID scheduleId) {
}
