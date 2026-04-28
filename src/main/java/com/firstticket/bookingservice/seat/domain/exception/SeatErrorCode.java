package com.firstticket.bookingservice.seat.domain.exception;

import com.firstticket.common.response.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SeatErrorCode implements ErrorCode {

    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 좌석입니다."),
    INVALID_SEAT_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 좌석ID입니다."),
    INVALID_SEAT(HttpStatus.BAD_REQUEST, "유효하지 않은 좌석입니다."),
    INVALID_SEAT_PRICE(HttpStatus.BAD_REQUEST, "유효하지 않은 좌석 가격입니다."),
    SEAT_ALREADY_HELD(HttpStatus.CONFLICT, "이미 선택된 좌석입니다."),
    SEAT_HOLD_FAILED(HttpStatus.CONFLICT, "이미 선택된 좌석입니다."),
    SEAT_NOT_AVAILABLE(HttpStatus.CONFLICT, "이미 예매된 좌석입니다."),
    SEAT_NOT_HELD(HttpStatus.CONFLICT, "선점 정보가 유효하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
