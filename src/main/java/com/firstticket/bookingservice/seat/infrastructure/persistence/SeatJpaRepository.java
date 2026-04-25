package com.firstticket.bookingservice.seat.infrastructure.persistence;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatJpaRepository extends JpaRepository<Seat, SeatId> {

    Optional<Seat> findByIdAndScheduleId(SeatId id, UUID scheduleId);

    List<Seat> findAllByScheduleId(UUID scheduleId);

    List<Seat> findAllByIdInAndScheduleId(List<SeatId> ids, UUID scheduleId);
}
