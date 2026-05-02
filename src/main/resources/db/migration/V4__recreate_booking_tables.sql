-- 기존 테이블 및 인덱스 제거
DROP INDEX IF EXISTS idx_booking_items_booking_id;
DROP INDEX IF EXISTS idx_bookings_user_id;
DROP INDEX IF EXISTS idx_bookings_status;

DROP TABLE IF EXISTS P_BOOKING_ITEM;
DROP TABLE IF EXISTS P_BOOKING;

-- P_BOOKING 재생성
CREATE TABLE P_BOOKING
(
    id            UUID          NOT NULL,
    session_id    TEXT          NOT NULL UNIQUE,
    user_id       UUID          NOT NULL,
    program_id    UUID          NOT NULL,
    schedule_id   UUID          NOT NULL,
    program_title VARCHAR(255)  NOT NULL,
    event_start_at TIMESTAMP    NOT NULL,
    event_end_at   TIMESTAMP    NOT NULL,
    venue_name    VARCHAR(255)  NOT NULL,
    venue_address VARCHAR(500)  NOT NULL,
    total_price   BIGINT        NOT NULL,
    total_count   INT           NOT NULL DEFAULT 0,
    status        VARCHAR(20)   NOT NULL,
    expired_at    TIMESTAMP,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    deleted_at    TIMESTAMP,
    created_by    UUID,
    updated_by    UUID,
    deleted_by    UUID,
    PRIMARY KEY (id)
);

-- P_BOOKING_ITEM 재생성
CREATE TABLE P_BOOKING_ITEM
(
    id            UUID         NOT NULL,
    booking_id    UUID         NOT NULL,
    seat_id       UUID         NOT NULL,
    seat_position VARCHAR(100) NOT NULL,
    price         BIGINT       NOT NULL,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    deleted_at    TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_booking_items_booking
        FOREIGN KEY (booking_id) REFERENCES P_BOOKING (id)
);

-- 인덱스 재생성
CREATE INDEX idx_bookings_status ON P_BOOKING (status);
CREATE INDEX idx_bookings_user_id ON P_BOOKING (user_id);
CREATE INDEX idx_bookings_session_id ON P_BOOKING (session_id);
CREATE INDEX idx_booking_items_booking_id ON P_BOOKING_ITEM (booking_id);
