DROP INDEX idx_seat_schedule_section;

CREATE INDEX idx_seat_schedule_id ON p_seat (schedule_id);
