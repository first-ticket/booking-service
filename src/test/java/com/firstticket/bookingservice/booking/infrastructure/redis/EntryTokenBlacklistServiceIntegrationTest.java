package com.firstticket.bookingservice.booking.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;

import com.firstticket.bookingservice.global.token.EntryTokenBlacklistService;
import com.redis.testcontainers.RedisContainer;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@SpringBootTest(classes = EntryTokenBlacklistService.class)
@Import(EntryTokenBlacklistServiceIntegrationTest.TestConfig.class)
@Testcontainers
class EntryTokenBlacklistServiceIntegrationTest {

    @Container
    static RedisContainer redis = new RedisContainer(
        DockerImageName.parse("redis:7-alpine")
    );

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RedissonClient redissonClient(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port
        ) {
            Config config = new Config();
            config.useSingleServer()
                .setAddress("redis://" + host + ":" + port);
            return Redisson.create(config);
        }
    }

    @Autowired
    private EntryTokenBlacklistService entryTokenBlacklistService;

    @Test
    void 최초_블랙리스트_등록시_true를_반환한다() {
        String token = "test-entry-token";
        Date expiration = new Date(System.currentTimeMillis() + 60000);

        boolean result = entryTokenBlacklistService.tryBlacklist(token, expiration);

        assertThat(result).isTrue();
    }

    @Test
    void 동일_토큰_재시도시_false를_반환한다() {
        String token = "duplicate-entry-token";
        Date expiration = new Date(System.currentTimeMillis() + 60000);

        entryTokenBlacklistService.tryBlacklist(token, expiration);
        boolean result = entryTokenBlacklistService.tryBlacklist(token, expiration);

        assertThat(result).isFalse();
    }

    @Test
    void 만료된_토큰은_등록하지_않고_false를_반환한다() {
        String token = "expired-entry-token";
        Date expiration = new Date(System.currentTimeMillis() - 1000);

        boolean result = entryTokenBlacklistService.tryBlacklist(token, expiration);

        assertThat(result).isFalse();
    }
}
