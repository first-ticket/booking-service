package com.firstticket.bookingservice.booking.application.port;

import java.util.UUID;

public interface PublishEvent {
    void paymentRefundEvent(UUID paymentId, UUID userId, UUID BookingId);
}
