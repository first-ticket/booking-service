package com.firstticket.bookingservice.booking.domain;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {

    // 예매 생성 순차 중복 방지 (이미 동일한 조건의 예매 레코드가 있는지 확인)
    // 동일 세션으로는 PNEDING 상태의 예매를 하나만 만들 수 있다. -> 이미 PAID 상태 이후부터는 동일 세션으로 예매 생성 불가, 아직 PENDING 상태이면 기존거 CANCELED로 바꾸고 새 예매 레코드 생성 가능
    boolean isDuplicated(String sessionId);

    void save(Booking booking);

    Optional<Booking> findById(UUID bookingId);

    UUID findIdBySessionId(String sessionId);

    Optional<Booking> findBySessionId(String sessionId);

    void hardDelete(Booking booking);
}
