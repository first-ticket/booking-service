package com.firstticket.bookingservice.seat.infrastructure.persistence;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryImpl implements SeatRepository {

    private final SeatJpaRepository jpaRepository;

    @Override
    public Optional<Seat> findByIdAndScheduleId(SeatId id, UUID scheduleId) {
        return jpaRepository.findByIdAndScheduleId(id, scheduleId);
    }

    @Override
    public List<Seat> findByScheduleId(UUID scheduleId) {
        return jpaRepository.findAllByScheduleId(scheduleId);
    }

    @Override
    public List<Seat> findAllByIdInAndScheduleId(List<SeatId> ids, UUID scheduleId) {
        return jpaRepository.findAllByIdInAndScheduleId(ids, scheduleId);
    }

    @Override
    public List<Seat> saveAll(List<Seat> seats) {
        return jpaRepository.saveAll(seats);
    }
}
