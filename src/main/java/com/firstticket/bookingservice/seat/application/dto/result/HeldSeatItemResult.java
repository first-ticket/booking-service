package com.firstticket.bookingservice.seat.application.dto.result;

import com.firstticket.bookingservice.seat.domain.Seat;

import java.util.UUID;

public record HeldSeatItemResult(
    UUID seatId,
    String seatInfo,
    int price
) {
    public static HeldSeatItemResult from(Seat seat) {
        return new HeldSeatItemResult(
            seat.getId().id(),
            seat.displayName(),
            seat.getPrice()
        );
    }
}
