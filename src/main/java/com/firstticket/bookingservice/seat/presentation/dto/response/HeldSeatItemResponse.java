package com.firstticket.bookingservice.seat.presentation.dto.response;

import com.firstticket.bookingservice.seat.application.dto.result.HeldSeatItemResult;

import java.util.UUID;

public record HeldSeatItemResponse(
    UUID seatId,
    String seatInfo,
    int price
) {
    public static HeldSeatItemResponse from(HeldSeatItemResult result) {
        return new HeldSeatItemResponse(
            result.seatId(),
            result.seatInfo(),
            result.price()
        );
    }
}
