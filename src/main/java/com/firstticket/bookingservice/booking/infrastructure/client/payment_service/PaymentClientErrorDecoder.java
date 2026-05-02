package com.firstticket.bookingservice.booking.infrastructure.client.payment_service;

import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class PaymentClientErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new BookingException(BookingErrorCode.PAYMENT_SERVICE_ERROR);
            default -> new FeignException.InternalServerError(
                "payment-service 오류", response.request(), null, null
            );
        };
    }
}
