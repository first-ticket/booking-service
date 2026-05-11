package com.firstticket.bookingservice.seat.domain;

import com.firstticket.bookingservice.seat.domain.query.SeatRemainingCount;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SeatRepository {

    void bulkInsert(List<Seat> seats);

    Optional<Seat> findByIdAndScheduleId(SeatId id, UUID scheduleId);

    List<Seat> findByScheduleId(UUID scheduleId);

    List<Seat> findAllByIdInAndScheduleId(List<SeatId> ids, UUID scheduleId);

    List<Seat> saveAll(List<Seat> seats);

    Set<SeatId> findHeldSeatIds(List<SeatId> seatIds);

    List<SeatRemainingCount> countAvailableByProgramId(UUID programId);

    List<Seat> findAllByIdIn(List<SeatId> ids);
}
