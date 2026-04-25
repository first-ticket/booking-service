package com.firstticket.bookingservice.seat.domain.exception;

import com.firstticket.common.response.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SeatErrorCode implements ErrorCode {

    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 좌석입니다"),
    INVALID_SEAT_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 좌석 ID 입니다");

    private final HttpStatus status;
    private final String message;
}
