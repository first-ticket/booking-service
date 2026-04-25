package com.firstticket.bookingservice.seat.domain.exception;

import com.firstticket.common.exception.BusinessException;
import com.firstticket.common.response.ErrorCode;

public class SeatException extends BusinessException {

    public SeatException(ErrorCode errorCode) {
        super(errorCode);
    }
}
