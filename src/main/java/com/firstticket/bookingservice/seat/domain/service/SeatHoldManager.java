package com.firstticket.bookingservice.seat.domain.service;

import com.firstticket.bookingservice.seat.domain.SeatId;

import java.util.List;
import java.util.UUID;

public interface SeatHoldManager {

    // 좌석 선점
    void hold(UUID scheduleId, List<SeatId> seatIds, UUID userId, String sessionId);

    // 좌석 선점 해제
    void releaseAll(UUID scheduleId, List<SeatId> seatIds, String sessionId);

    // 좌석 선점 여부 확인
    boolean isHeld(List<SeatId> seatIds, UUID userId, String sessionId);

    // 선점된 좌석 아이디 조회
    List<SeatId> getHeldSeatIds(String sessionId, UUID scheduleId);
}
