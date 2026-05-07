package com.firstticket.bookingservice.booking.domain.service;

import java.util.List;
import java.util.UUID;

public interface PublishEvent {
    void paymentRefundEvent(UUID paymentId, UUID userId, UUID BookingId);
    void cancelBooking(UUID paymentId, UUID userId, UUID bookingId);
    void cancelBookingConfirmed(List<UUID> seatList, UUID bookingId);
}
