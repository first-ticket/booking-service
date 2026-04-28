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

        // VO 불변식 검증 : null과 음수 금액 불허용
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("금액은 null이 아닌 0 이상의 값이어야 합니다.");
        }

        this.amount = amount;
    }
}
