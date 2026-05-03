package com.firstticket.bookingservice.booking.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.firstticket.bookingservice.booking.domain.vo.Money;
import org.junit.jupiter.api.Test;

public class MoneyTest {
    @Test
    void plus_호출시_기존_객체는_변경되지_않고_새로운_객체를_만든다(){
        Money originalMoney = new Money(1000L);
        Money result = originalMoney.plus(500L);

        assertThat(originalMoney.getAmount() == 1000L);
        assertThat(result.getAmount() == 1500L);
        assertThat(!result.equals(originalMoney));
    }
}
