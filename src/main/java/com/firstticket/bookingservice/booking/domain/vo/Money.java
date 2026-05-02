package com.firstticket.bookingservice.booking.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class Money {
    private Long amount;

    public Money(Long amount) {
        this.amount = amount;
    }

    public Money plus(Long price){
        return new Money(this.amount + price);
    }
}
