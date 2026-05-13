package com.firstticket.bookingservice.seat.infrastructure.redis;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.infrastructure.persistence.SeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeatCacheManager {

    private final SeatJpaRepository jpaRepository;

    @CachePut(value = "seats", key = "#scheduleId")
    public List<Seat> refreshSeatCache(UUID scheduleId) {
        return jpaRepository.findAllByScheduleId(scheduleId);
    }
}
