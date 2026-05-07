package com.firstticket.bookingservice.booking.domain.service.dto;

import java.util.UUID;

public record HeldSeatResult(
    UUID seatId,
    String seatInfo,
    Long price
) {
}
