package com.firstticket.bookingservice.seat.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatRepository {

    Optional<Seat> findByIdAndScheduleId(SeatId id, UUID scheduleId);

    List<Seat> findByScheduleId(UUID scheduleId);

    List<Seat> findAllByIdInAndScheduleId(List<SeatId> ids, UUID scheduleId);

    List<Seat> saveAll(List<Seat> seats);
}
