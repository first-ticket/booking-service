package com.firstticket.bookingservice.seat.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record SeatPosition(
    String zone,
    int rowNum,
    int colNum
) {
    public static SeatPosition of(String zone, int rowNum, int colNum) {
        return new SeatPosition(zone, rowNum, colNum);
    }
}
