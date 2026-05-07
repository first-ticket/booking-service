package com.firstticket.bookingservice.booking.domain.service;

import com.firstticket.bookingservice.booking.domain.service.dto.PaymentResult;
import java.util.UUID;

public interface PaymentOperator {
    PaymentResult createPayment(UUID bookingId, UUID userId, Long amount);
}
