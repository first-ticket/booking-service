package com.firstticket.bookingservice.booking.domain.vo;

import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class Money {
    private Long amount;

    public Money(Long amount) {
        if(amount == null || amount < 0){
            throw new BookingException(BookingErrorCode.INVALID_PRICE);
        }
        this.amount = amount;
    }

    public Money plus(Long price){
        if(price == null){
            throw new BookingException(BookingErrorCode.INVALID_PRICE);
        }
        return new Money(this.amount + price);
    }
}
