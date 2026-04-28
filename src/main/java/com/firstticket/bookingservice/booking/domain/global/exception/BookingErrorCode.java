package com.firstticket.bookingservice.booking.domain.global.exception;

import com.firstticket.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum BookingErrorCode implements ErrorCode {
    ;

    @Override
    public HttpStatus getStatus() {
        return null;
    }

    @Override
    public String getMessage() {
        return "";
    }
}
