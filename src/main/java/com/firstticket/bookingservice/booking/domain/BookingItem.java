package com.firstticket.bookingservice.booking.domain;

import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import com.firstticket.bookingservice.booking.domain.vo.Money;
import com.firstticket.common.persistence.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "P_BOOKING_ITEM")
public class BookingItem extends BaseEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "booking_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Booking booking;

    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    @Column(name = "seat_position", nullable = false)
    private String seatPosition;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false)),
    })
    private Money price;


    private BookingItem(
        Booking booking,
        UUID seatId,
        String seatPosition,
        Money price
    ){
        this.booking = booking;
        this.seatId = seatId;
        this.seatPosition = seatPosition;
        this.price = price;
    }

    static BookingItem of(
        Booking booking,
        UUID seatId,
        String seatPosition,
        Money price
    ){
        Objects.requireNonNull(booking, "booking은 null일 수 없습니다.");
        Objects.requireNonNull(seatId, "seatId는 null일 수 없습니다.");
        Objects.requireNonNull(price, "price는 null일 수 없습니다.");

        // 문자열 공백 및 null 체크
        if (seatPosition == null || seatPosition.isBlank()) {
            throw new BookingException(BookingErrorCode.INVALID_SEAT_POSITION);
        }

        return new BookingItem(
            booking,
            seatId,
            seatPosition,
            price
        );
    }
}
