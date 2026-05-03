package com.firstticket.bookingservice.booking.application.port;

import com.firstticket.bookingservice.booking.application.port.dto.PaymentResult;
import java.util.UUID;

public interface PaymentOperator {
    PaymentResult createPayment(UUID bookingId, UUID userId, Long amount);
}
