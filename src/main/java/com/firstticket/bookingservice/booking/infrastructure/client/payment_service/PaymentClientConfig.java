package com.firstticket.bookingservice.booking.infrastructure.client.payment_service;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class PaymentClientConfig {
    @Bean
    public ErrorDecoder errorDecoder(){return new PaymentClientErrorDecoder();}
}
