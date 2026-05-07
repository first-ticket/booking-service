package com.firstticket.bookingservice.booking.domain.service.vo;

import java.util.UUID;

public record HeldSeatResult(
    UUID seatId,
    String seatInfo,
    Long price
) {
}
