package com.firstticket.bookingservice.seat.infrastructure.persistence;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.SeatRepository;
import com.firstticket.bookingservice.seat.domain.SeatType;
import com.firstticket.bookingservice.seat.domain.query.SeatRemainingCount;
import com.firstticket.bookingservice.seat.infrastructure.redis.SeatRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryImpl implements SeatRepository {

    private final SeatJpaRepository jpaRepository;
    private final SeatRedisRepository redisRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 500;

    @Transactional
    @Override
    public void bulkInsert(List<Seat> seats) {
        String seatedSql = """
                INSERT INTO booking_schema.p_seat
                    (id, program_id, schedule_id, section_id, section_name,
                     seat_type, status, price, row_num, col_num, created_at)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String standingSql = """
                INSERT INTO booking_schema.p_seat
                    (id, program_id, schedule_id, section_id, section_name,
                     seat_type, status, price, entry_num, created_at)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        List<Seat> seatedSeats = seats.stream()
            .filter(s -> s.getSeatType() == SeatType.SEATED)
            .toList();

        List<Seat> standingSeats = seats.stream()
            .filter(s -> s.getSeatType() == SeatType.STANDING)
            .toList();

        LocalDateTime now = LocalDateTime.now();

        partition(seatedSeats, BATCH_SIZE).forEach(batch ->
            jdbcTemplate.batchUpdate(seatedSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Seat seat = batch.get(i);
                    ps.setObject(1, seat.getId().id());
                    ps.setObject(2, seat.getProgramId());
                    ps.setObject(3, seat.getScheduleId());
                    ps.setObject(4, seat.getSection().sectionId());
                    ps.setString(5, seat.getSection().sectionName());
                    ps.setString(6, seat.getSeatType().name());
                    ps.setString(7, seat.getStatus().name());
                    ps.setInt(8, seat.getPrice());
                    ps.setInt(9, seat.getSeatedInfo().rowNum());
                    ps.setInt(10, seat.getSeatedInfo().colNum());
                    ps.setTimestamp(11, Timestamp.valueOf(now));
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            })
        );

        partition(standingSeats, BATCH_SIZE).forEach(batch ->
            jdbcTemplate.batchUpdate(standingSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Seat seat = batch.get(i);
                    ps.setObject(1, seat.getId().id());
                    ps.setObject(2, seat.getProgramId());
                    ps.setObject(3, seat.getScheduleId());
                    ps.setObject(4, seat.getSection().sectionId());
                    ps.setString(5, seat.getSection().sectionName());
                    ps.setString(6, seat.getSeatType().name());
                    ps.setString(7, seat.getStatus().name());
                    ps.setInt(8, seat.getPrice());
                    ps.setInt(9, seat.getStandingInfo().entryNum());
                    ps.setTimestamp(10, Timestamp.valueOf(now));
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            })
        );

    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    @Override
    public Optional<Seat> findByIdAndScheduleId(SeatId id, UUID scheduleId) {
        return jpaRepository.findByIdAndScheduleId(id, scheduleId);
    }

    @Cacheable(value = "seats", key = "#scheduleId")
    @Override
    public List<Seat> findByScheduleId(UUID scheduleId) {
        return jpaRepository.findAllByScheduleId(scheduleId);
    }

    @CachePut(value = "seats", key = "#scheduleId")
    @Override
    public List<Seat> refreshSeatCache(UUID scheduleId) {
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

    @Override
    public List<Seat> findAllByIdIn(List<SeatId> seatIds) {
        return jpaRepository.findAllByIdIn(seatIds);
    }

}
