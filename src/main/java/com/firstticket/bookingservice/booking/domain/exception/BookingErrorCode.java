package com.firstticket.bookingservice.booking.domain.exception;

import com.firstticket.common.response.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum BookingErrorCode implements ErrorCode {
    IMPOSSIBLE_STATE_TRANSITION(HttpStatus.CONFLICT, "허용되지 않는 상태 전이입니다"),

    EMPTY_SEAT_POSITION(HttpStatus.BAD_REQUEST, "좌석 정보는 필수입니다"),

    EXPIRED_ENTRY_TOKEN(HttpStatus.UNAUTHORIZED, "예매 입장 토큰이 만료되었습니다"),
    INVALID_ENTRY_TOKEN(HttpStatus.UNAUTHORIZED, "예매 입장 토큰이 유효하지 않습니다"),
    BLACKLISTED_ENTRY_TOKEN(HttpStatus.UNAUTHORIZED, "예매 입장 토큰이 유효하지 않습니다"),

    EXPIRED_SESSION_TOKEN(HttpStatus.UNAUTHORIZED, "예매 세션 토큰이 만료되었습니다"),
    INVALID_SESSION_TOKEN(HttpStatus.UNAUTHORIZED, "예매 세션 토큰이 유효하지 않습니다"),
    EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "예매 토큰이 존재하지 않습니다"),

    INVALID_USER_ID(HttpStatus.FORBIDDEN, "토큰의 사용자 정보가 요청자와 일치하지 않습니다"),
    INVALID_PROGRAM_ID(HttpStatus.FORBIDDEN, "토큰의 프로그램 정보가 요청 경로와 일치하지 않습니다"),
    EMPTY_X_USER_ID(HttpStatus.BAD_REQUEST, "X-User-Id 헤더가 누락되었습니다"),
    EMPTY_PATHVARIABLE(HttpStatus.BAD_REQUEST, "프로그램 ID가 요청 경로에 포함되지 않았습니다");
    ;

    private final HttpStatus status;
    private final String message;
}
