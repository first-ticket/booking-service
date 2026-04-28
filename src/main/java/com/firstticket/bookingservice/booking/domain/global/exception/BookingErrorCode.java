package com.firstticket.bookingservice.booking.domain.global.exception;

import com.firstticket.common.response.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum BookingErrorCode implements ErrorCode {
    IMPOSSIBLE_STATE_TRANSITION(HttpStatus.FORBIDDEN, "[Booking domain]_허용되지 않는 상태 전이 입니다"),
    EMPTY_SEAT_POSITION(HttpStatus.FORBIDDEN, "[Booking domain]_좌석 정보는 필수입니다");

    private final HttpStatus status;
    private final String message;
}
