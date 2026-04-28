package com.firstticket.bookingservice.booking.domain;

import java.util.Map;
import java.util.Set;

public enum BookingStatus {
    PENDING,
    PAID,
    CONFIRMED,
    CANCELED;

    //static final은 관례상 변수명을 대문자로 사용
    private static final Map<BookingStatus, Set<BookingStatus>> TRANSITIONS = Map.of(
        PENDING, Set.of(PAID, CANCELED),
        PAID, Set.of(CONFIRMED, CANCELED),
        CONFIRMED, Set.of(),
        CANCELED, Set.of()
    );

    public BookingStatus validateTransition(BookingStatus nextStatus){
        if(!TRANSITIONS.get(this).contains(nextStatus)){
            throw new IllegalStateException(
                this + " -> " + nextStatus + ": 허용되지 않는 상태 전이 입니다."
            );
        }
        return nextStatus;
    }
}
