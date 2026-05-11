package com.firstticket.bookingservice.booking.infrastructure.messaging.payload;

import java.util.List;
import java.util.UUID;

public record BookingCancelConfirmedPayload(
    List<UUID> seatList,
    UUID scheduleId
) {
    public static BookingCancelConfirmedPayload of(List<UUID> seatList, UUID scheduleId){
        return new BookingCancelConfirmedPayload(seatList, scheduleId);
    }
}
