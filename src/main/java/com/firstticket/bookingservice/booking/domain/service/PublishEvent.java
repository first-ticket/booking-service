package com.firstticket.bookingservice.booking.domain.service;

import java.util.UUID;

public interface PublishEvent {
    void paymentRefundEvent(UUID paymentId, UUID userId, UUID BookingId);
}
