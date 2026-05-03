package com.firstticket.bookingservice.booking.infrastructure.client.payment_service;

import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class PaymentClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        if (status >= 400 && status < 500) {
            return new BookingException(BookingErrorCode.PAYMENT_SERVICE_CLIENT_ERROR);
        }

        return new BookingException(BookingErrorCode.PAYMENT_SERVICE_SERVER_ERROR);
    }
}
