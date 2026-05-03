package com.firstticket.bookingservice.booking.infrastructure.client.program_service;

import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class ProgramClientErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400 -> new BookingException(BookingErrorCode.INVALID_SCHEDULE_ID);
            case 404 -> new BookingException(BookingErrorCode.INVALID_SCHEDULE_ID);
            default -> new BookingException(BookingErrorCode.PROGRAM_SERVICE_ERROR);
        };
    }
}
