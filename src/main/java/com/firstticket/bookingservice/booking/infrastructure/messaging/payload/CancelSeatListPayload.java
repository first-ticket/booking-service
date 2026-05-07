package com.firstticket.bookingservice.booking.infrastructure.messaging.payload;

import java.util.List;
import java.util.UUID;

public record CancelSeatListPayload(
    List<UUID> seatList
) {
    public static CancelSeatListPayload of(List<UUID> seatList){
        return new CancelSeatListPayload(seatList);
    }
}
