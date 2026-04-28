package com.firstticket.bookingservice.booking.domain.global.exception;

import com.firstticket.common.exception.BusinessException;
import com.firstticket.common.response.ErrorCode;

public class BookingException extends BusinessException {
    public BookingException(ErrorCode errorCode) {
        super(errorCode);
    }
}
