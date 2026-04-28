CREATE TABLE P_BOOKING
(
    id           UUID         NOT NULL,
    user_id      UUID         NOT NULL,
    schedule_id  UUID         NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    total_price  BIGINT       NOT NULL,
    expired_at   TIMESTAMP,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    deleted_at   TIMESTAMP,
    created_by   UUID,
    updated_by   UUID,
    deleted_by   UUID,
    PRIMARY KEY (id)
);

CREATE TABLE P_BOOKING_ITEM
(
    id            UUID        NOT NULL,
    booking_id    UUID        NOT NULL,
    seat_id       UUID        NOT NULL,
    seat_position VARCHAR(50) NOT NULL,
    price         BIGINT      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_booking_items_booking
        FOREIGN KEY (booking_id) REFERENCES P_BOOKING (id)
);

-- 만료 스케줄러: PENDING 상태 + createdAt 기준 조회
CREATE INDEX idx_bookings_status ON P_BOOKING (status);
-- 예매 조회: userId 기준
CREATE INDEX idx_bookings_user_id ON P_BOOKING (user_id);
-- BookingItem 조인 조회
CREATE INDEX idx_booking_items_booking_id ON P_BOOKING_ITEM (booking_id);
