package com.firstticket.bookingservice.seat.infrastructure.redis;

import com.firstticket.bookingservice.seat.domain.SeatId;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SeatRedisRepository {

    private final RedissonClient redissonClient;

    public Set<SeatId> findHeldSeatIds(List<SeatId> seatIds) {
        List<String> keys = seatIds.stream()
            .map(this::holdKey)
            .toList();

        Map<String, String> buckets = redissonClient.getBuckets().get(keys.toArray(new String[0]));

        return seatIds.stream()
            .filter(id -> buckets.containsKey(holdKey(id)))
            .collect(Collectors.toSet());
    }

    private String holdKey(SeatId seatId) {
        return "held:" + seatId.id();
    }
}
