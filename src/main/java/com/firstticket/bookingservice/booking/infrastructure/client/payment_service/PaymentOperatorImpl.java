package com.firstticket.bookingservice.booking.infrastructure.client.payment_service;

import com.firstticket.bookingservice.booking.application.port.PaymentOperator;
import com.firstticket.bookingservice.booking.application.port.dto.PaymentResult;
import com.firstticket.bookingservice.booking.infrastructure.client.payment_service.dto.PaymentRequest;
import com.firstticket.bookingservice.booking.infrastructure.client.payment_service.dto.PaymentResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentOperatorImpl implements PaymentOperator {

    private final PaymentClient paymentClient;

    @Override
    public PaymentResult createPayment(UUID bookingId, UUID userId, Long amount) {
        PaymentResponse response = paymentClient.createPayment(new PaymentRequest(bookingId, userId, amount));
        return new PaymentResult(response.paymentId(), response.orderId(), response.amount());
    }
}
