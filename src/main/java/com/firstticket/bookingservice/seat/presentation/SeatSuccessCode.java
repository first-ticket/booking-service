package com.firstticket.bookingservice.seat.presentation;

import com.firstticket.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SeatSuccessCode implements SuccessCode {

    SEAT_HELD(HttpStatus.OK, "좌석 선점이 완료되었습니다.");

    private final HttpStatus status;
    private final String message;
}
