package com.firstticket.bookingservice.booking.infrastructure.messaging.payload;

import java.util.List;
import java.util.UUID;

public record BookingCancelConfirmedPayload(
    List<UUID> seatList
) {
    public static BookingCancelConfirmedPayload of(List<UUID> seatList){
        return new BookingCancelConfirmedPayload(seatList);
    }
}
