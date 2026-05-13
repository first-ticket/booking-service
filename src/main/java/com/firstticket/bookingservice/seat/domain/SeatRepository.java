package com.firstticket.bookingservice.seat.domain;

import com.firstticket.bookingservice.seat.domain.query.SeatRemainingCount;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface SeatRepository {

    void bulkInsert(List<Seat> seats);


    List<Seat> findByScheduleId(UUID scheduleId);

    List<Seat> refreshSeatCache(UUID scheduleId);

    List<Seat> findAllByIdInAndScheduleId(List<SeatId> ids, UUID scheduleId);


    Set<SeatId> findHeldSeatIds(List<SeatId> seatIds);

    List<SeatRemainingCount> countAvailableByProgramId(UUID programId);

}
