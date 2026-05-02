package com.firstticket.bookingservice.booking.application.port.dto;

import java.util.UUID;

public record HeldSeatResult(
    UUID seatId,
    String seatInfo,
    Long price
) {
}
