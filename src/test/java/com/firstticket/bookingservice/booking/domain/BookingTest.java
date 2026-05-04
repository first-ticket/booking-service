package com.firstticket.bookingservice.booking.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingTest {

    private Booking booking;

    @BeforeEach
    void setUp() {
        booking = Booking.create(
            UUID.randomUUID(),
            "test-session-id",
            UUID.randomUUID(),
            UUID.randomUUID(),
            "테스트 공연",
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(10).plusHours(2),
            "올림픽공원",
            "서울시 송파구"
        );
    }

    @Test
    void addItem_호출시_totalCount와_totalPrice가_누적된다() {
        booking.addItem(UUID.randomUUID(), "A구역 1열 1번", 10000L);
        booking.addItem(UUID.randomUUID(), "A구역 1열 2번", 15000L);

        assertThat(booking.getTotalCount()).isEqualTo(2);
        assertThat(booking.getTotalPrice().getAmount()).isEqualTo(25000L);
    }

    @Test
    void paid_호출시_PENDING에서_PAID로_전이된다() {
        booking.paid();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAID);
    }

    @Test
    void confirm_호출시_PAID에서_CONFIRMED로_전이된다() {
        booking.paid();
        booking.confirm();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void cancel_호출시_PENDING에서_CANCELED로_전이되고_expiredAt이_세팅된다() {
        booking.cancel();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELED);
        assertThat(booking.getExpiredAt()).isNotNull();
    }

    @Test
    void CONFIRMED_상태에서_전이_시도시_예외가_발생한다() {
        booking.paid();
        booking.confirm();

        assertThatThrownBy(() -> booking.cancel())
            .isInstanceOf(BookingException.class);
    }

    @Test
    void CANCELED_상태에서_전이_시도시_예외가_발생한다() {
        booking.cancel();

        assertThatThrownBy(() -> booking.paid())
            .isInstanceOf(BookingException.class);
    }
}
