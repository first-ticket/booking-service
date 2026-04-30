package com.firstticket.bookingservice.seat.domain;

public enum SeatStatus {
    AVAILABLE,
    RESERVED,
    HELD // Redis TTL로만 관리, DB에 저장 X
}
