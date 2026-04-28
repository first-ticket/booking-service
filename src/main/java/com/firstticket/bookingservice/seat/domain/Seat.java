package com.firstticket.bookingservice.seat.domain;

import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import com.firstticket.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 좌석 도메인
 *
 * 프로그램 회차별 예매 가능한 좌석을 나타내는 애그리거트 루트
 * VenueSeat(공연장 고정 좌석)를 기반으로 스냅샷 방식으로 생성되며
 * 이후 VenueSeat 변경에 영향을 받지 않고 독립적으로 관리된다
 *
 * 좌석 상태는 DB에서 AVAILABLE/RESERVED로 관리되며
 * 선점 상태(HELD)는 Redis TTL로만 관리하고 DB에 저장하지 않는다
 *
 * 생성은 정적 팩토리 메서드 create()를 통해서만 가능하며
 * 초기 상태는 항상 AVAILABLE이다
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Seat extends BaseEntity {

    @EmbeddedId
    private SeatId id;

    @Column(nullable = false)
    private UUID scheduleId;

    @Embedded
    private SeatPosition position;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    public static Seat create(UUID scheduleId, SeatPosition position, int price) {
        if (scheduleId == null) {
            throw new SeatException(SeatErrorCode.INVALID_SEAT);
        }
        if (position == null) {
            throw new SeatException(SeatErrorCode.INVALID_SEAT);
        }
        if (price < 0) {
            throw new SeatException(SeatErrorCode.INVALID_SEAT_PRICE);
        }

        return new Seat(
            SeatId.of(),
            scheduleId,
            position,
            price,
            SeatStatus.AVAILABLE
        );
    }

    public void reserve() {
        if (this.status == SeatStatus.RESERVED) {
            throw new SeatException(SeatErrorCode.SEAT_ALREADY_RESERVED);
        }
        this.status = SeatStatus.RESERVED;
    }

    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }
}
