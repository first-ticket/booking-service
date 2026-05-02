package com.firstticket.bookingservice.booking.infrastructure.client.payment_service;

import com.firstticket.bookingservice.booking.infrastructure.client.payment_service.dto.PaymentRequest;
import com.firstticket.bookingservice.booking.infrastructure.client.payment_service.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", path = "/internal/v1/payments", configuration = PaymentClientConfig.class)
public interface PaymentClient {

    @PostMapping
    PaymentResponse createPayment(@RequestBody PaymentRequest paymentRequest);
}
