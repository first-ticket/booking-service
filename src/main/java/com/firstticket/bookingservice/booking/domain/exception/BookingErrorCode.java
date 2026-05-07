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
    INVALID_SEAT_POSITION(HttpStatus.BAD_REQUEST, "올바르지 않은 좌석 위치입니다"),

    EXPIRED_ENTRY_TOKEN(HttpStatus.UNAUTHORIZED, "예매 입장 토큰이 만료되었습니다"),
    INVALID_ENTRY_TOKEN(HttpStatus.UNAUTHORIZED, "예매 입장 토큰이 유효하지 않습니다"),
    BLACKLISTED_ENTRY_TOKEN(HttpStatus.UNAUTHORIZED, "이미 사용 완료한 예매 입장 토큰입니다"),

    EXPIRED_SESSION_TOKEN(HttpStatus.UNAUTHORIZED, "예매 세션 토큰이 만료되었습니다"),
    INVALID_SESSION_TOKEN(HttpStatus.UNAUTHORIZED, "예매 세션 토큰이 유효하지 않습니다"),
    EMPTY_SESSION_TOKEN(HttpStatus.UNAUTHORIZED, "예매 세션 토큰이 존재하지 않습니다"),

    INVALID_USER_ID(HttpStatus.FORBIDDEN, "토큰의 사용자 정보가 요청자와 일치하지 않습니다"),
    INVALID_PROGRAM_ID(HttpStatus.FORBIDDEN, "토큰의 프로그램 정보가 요청 경로와 일치하지 않습니다"),
    EMPTY_X_USER_ID(HttpStatus.BAD_REQUEST, "X-User-Id 헤더가 누락되었습니다"),
    EMPTY_PATHVARIABLE(HttpStatus.BAD_REQUEST, "프로그램 ID가 요청 경로에 포함되지 않았습니다"),

    BOOKING_LOCK_FAILED(HttpStatus.CONFLICT, "동일한 예매 요청이 이미 처리 중입니다"),

    INVALID_PRICE(HttpStatus.BAD_REQUEST,"금액은 null이 아닌 0 이상의 값이어야 합니다"),
    INVALID_PROGRAM_TITLE(HttpStatus.BAD_REQUEST,"프로그램 제목은 필수입니다"),
    INVALID_EVENT_TIME(HttpStatus.BAD_REQUEST,"프로그램 시작 시간과 종료 시간이 올바르지 않습니다"),
    INVALID_VENUE_NAME(HttpStatus.BAD_REQUEST,"장소 이름은 필수입니다"),
    INVALID_VENUE_ADDRESS(HttpStatus.BAD_REQUEST,"장소 주소는 필수입니다"),

    INVALID_SEAT_ID(HttpStatus.BAD_REQUEST, "해당 좌석을 선점한 상태가 아닙니다" ),

    INVALID_SCHEDULE_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 스케줄 ID 입니다" ),
    INVALID_SCHEDULE_PERIOD(HttpStatus.BAD_REQUEST, "해당 프로그램을 예매할 수 있는 기간이 아닙니다" ),

    DUPLICATE_BOOKING(HttpStatus.BAD_REQUEST,"동일한 예매 요청이 이미 진행중 입니다" ),
    INVALID_SESSION_ID(HttpStatus.BAD_REQUEST, "세션 아이디는 필수입니다"),

    PAYMENT_SERVICE_ERROR(HttpStatus.NOT_FOUND, "결제 서비스 에러" ),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "전달받은 결제 STATUS가 SUCCESS도, FAILED도 아닙니다"),

    INVALID_BOOKING_ID(HttpStatus.NOT_FOUND, "해당하는 예매 데이터가 없습니다" ),
    INVALID_AUTHORIZATION(HttpStatus.UNAUTHORIZED, "조회 권한이 없습니다" ),
    INVALID_BOOKING_STATUS(HttpStatus.BAD_REQUEST,"유효한 예매 Status가 아닙니다" ),
    PROGRAM_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "프로그램 서비스 에러"),
    PAYMENT_SERVICE_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "결제 서비스 클라이언트 오류"),
    PAYMENT_SERVICE_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 서비스 서버 오류"),

    TAMPERED_ENTRY_TOKEN(HttpStatus.UNAUTHORIZED, "예매 입장 토큰이 변조되었습니다"),
    MALFORMED_ENTRY_TOKEN(HttpStatus.UNAUTHORIZED, "예매 입장 토큰 형식이 올바르지 않습니다");

    private final HttpStatus status;
    private final String message;
}
