package com.firstticket.bookingservice.booking.domain;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {

    // 예매 생성 순차 중복 방지 (이미 동일한 조건의 예매 레코드가 있는지 확인)
    // 동일 세션으로는 상태 무관하게 예매 1회만 허용
    boolean isDuplicated(String sessionId);

    void save(Booking booking);

    Optional<Booking> findById(UUID bookingId);

    UUID findIdBySessionId(String sessionId);
}
