package com.firstticket.bookingservice.seat.domain;


import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record SeatId(
    UUID id
) {
    public SeatId {
        if (id == null) {
            throw new SeatException(SeatErrorCode.INVALID_SEAT_ID);
        }
    }

    public static SeatId of() {
        return SeatId.of(UUID.randomUUID());
    }

    public static SeatId of(UUID id) {
        return new SeatId(id);
    }
}
