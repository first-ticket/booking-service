package com.firstticket.bookingservice.seat.infrastructure.persistence;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.query.SeatRemainingCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatJpaRepository extends JpaRepository<Seat, SeatId> {

    Optional<Seat> findByIdAndScheduleId(SeatId id, UUID scheduleId);

    List<Seat> findAllByScheduleId(UUID scheduleId);

    List<Seat> findAllByIdInAndScheduleId(List<SeatId> ids, UUID scheduleId);

    @Query(
        "SELECT new com.firstticket.bookingservice.seat.domain.query.SeatRemainingCount(s.scheduleId, COUNT(s)) " +
            "FROM Seat s " +
            "WHERE s.programId = :programId " +
            "AND s.status = com.firstticket.bookingservice.seat.domain.SeatStatus.AVAILABLE " +
            "GROUP BY s.scheduleId"
    )
    List<SeatRemainingCount> countAvailableByProgramId(@Param("programId") UUID programId);

    List<Seat> findAllByIdIn(List<SeatId> seatIds);
}
