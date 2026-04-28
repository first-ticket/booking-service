package com.firstticket.bookingservice.global.token;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EntryTokenBlacklistService {

    private final RedissonClient redissonClient;
    private static final String PREFIX = "blacklist:entry:";

    public void blacklist(String token, Date expirationDate){
        long remainingTtlMillis = expirationDate.getTime() - System.currentTimeMillis();
        // 남은 시간이 0보다 클 때만 저장 (이미 만료된 건 저장할 필요 없음)
        if (remainingTtlMillis > 0) {
            RBucket<String> bucket = redissonClient.getBucket(PREFIX + token);
            bucket.set("invalid", remainingTtlMillis, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isBlacklisted(String token){
        RBucket<String> bucket = redissonClient.getBucket(PREFIX + token);
        return bucket.isExists(); // 키가 존재하면 블랙리스트에 있는 것
    }
}
