package com.firstticket.bookingservice.seat.domain;

import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record StandingInfo(

    @Column(name = "entry_num")
    Integer entryNum

) {

    public static StandingInfo of(Integer entryNum) {
        if (entryNum == null || entryNum < 1) {
            throw new SeatException(SeatErrorCode.INVALID_SEAT);
        }
        return new StandingInfo(entryNum);
    }

    public String display(String sectionName) {
        return "%s구역 %d번".formatted(sectionName, entryNum);
    }
}
