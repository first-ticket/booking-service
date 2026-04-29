package com.firstticket.bookingservice.global.token;

import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EntryTokenBlacklistService {

    private final RedissonClient redissonClient;
    private static final String PREFIX = "blacklist:entry:";

    // EntryTokenBlacklistService
    public boolean tryBlacklist(String token, Date expirationDate) {
        long ttl = expirationDate.getTime() - System.currentTimeMillis();
        if (ttl <= 0) return false;

        RBucket<String> bucket = redissonClient.getBucket(PREFIX + token);
        return bucket.setIfAbsent("invalid", Duration.ofMillis(ttl)); // blacklist:entry:토큰 키가 있는지 확인 -> 값에 invalid 넣고, ttl 저장
        // true  → 최초 등록 성공 → 진행
        // false → 이미 존재 → 중복 요청
    }
}
