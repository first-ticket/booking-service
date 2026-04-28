package com.firstticket.bookingservice.booking.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Money {
    private Long amount;

    protected Money() {}

    public Money(Long amount) {
        this.amount = amount;
    }
}
