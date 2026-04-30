package com.firstticket.bookingservice.seat.infrastructure.persistence;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.SeatRepository;
import com.firstticket.bookingservice.seat.domain.query.SeatRemainingCount;
import com.firstticket.bookingservice.seat.infrastructure.redis.SeatRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryImpl implements SeatRepository {

    private final SeatJpaRepository jpaRepository;
    private final SeatRedisRepository redisRepository;

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

    @Override
    public Set<SeatId> findHeldSeatIds(List<SeatId> seatIds) {
        return redisRepository.findHeldSeatIds(seatIds);
    }

    @Override
    public List<SeatRemainingCount> countAvailableByProgramId(UUID programId) {
        return jpaRepository.countAvailableByProgramId(programId);
    }

}
