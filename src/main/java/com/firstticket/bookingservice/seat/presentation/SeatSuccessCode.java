package com.firstticket.bookingservice.seat.presentation;

import com.firstticket.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SeatSuccessCode implements SuccessCode {

    SEAT_HELD(HttpStatus.OK, "좌석 선점이 완료되었습니다."),
    SEAT_RELEASED(HttpStatus.OK, "좌석 선점이 해제되었습니다."),
    SEAT_HOLD_VALID(HttpStatus.OK, "좌석 선점이 유효합니다."),
    SEAT_RESERVED(HttpStatus.OK, "좌석이 확정되었습니다.");

    private final HttpStatus status;
    private final String message;
}
