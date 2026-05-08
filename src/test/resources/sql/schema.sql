CREATE SCHEMA IF NOT EXISTS booking_schema;

CREATE TABLE booking_schema.P_SEAT
(
    id           UUID        NOT NULL,
    program_id   UUID        NOT NULL,
    schedule_id  UUID        NOT NULL,
    section_id   UUID,
    section_name VARCHAR(50) NOT NULL,
    seat_type    VARCHAR(20) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    price        INT         NOT NULL,
    row_num      INT,
    col_num      INT,
    entry_num    INT,
    created_at   TIMESTAMP   NOT NULL,
    updated_at   TIMESTAMP,
    deleted_at   TIMESTAMP,

    CONSTRAINT pk_seat PRIMARY KEY (id),
    CONSTRAINT chk_seat_type CHECK (seat_type IN ('SEATED', 'STANDING')),
    CONSTRAINT chk_seat_status CHECK (status IN ('AVAILABLE', 'RESERVED')),
    CONSTRAINT chk_seated_info CHECK (
        (seat_type = 'SEATED' AND row_num IS NOT NULL AND col_num IS NOT NULL AND entry_num IS NULL)
            OR
        (seat_type = 'STANDING' AND entry_num IS NOT NULL AND row_num IS NULL AND col_num IS NULL)
        )
);

CREATE INDEX idx_seat_schedule_section ON booking_schema.p_seat (schedule_id, section_name);
