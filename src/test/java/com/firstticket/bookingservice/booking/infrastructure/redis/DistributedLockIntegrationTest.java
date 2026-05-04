package com.firstticket.bookingservice.booking.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;

import com.firstticket.bookingservice.booking.application.lock.DistributedLock;
import com.firstticket.bookingservice.booking.infrastructure.lock.AopForTransaction;
import com.firstticket.bookingservice.booking.infrastructure.lock.CustomSpringElParser;
import com.firstticket.bookingservice.booking.infrastructure.lock.DistributedLockAspect;
import com.redis.testcontainers.RedisContainer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Component;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(classes = {
    DistributedLockIntegrationTest.TestLockService.class,
    DistributedLockIntegrationTest.TestConfig.class,
    DistributedLockAspect.class,
    AopForTransaction.class,
    CustomSpringElParser.class
})
@EnableAspectJAutoProxy
@Testcontainers
class DistributedLockIntegrationTest {

    @Container
    static RedisContainer redis = new RedisContainer(
        DockerImageName.parse("redis:7-alpine")
    );

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private TestLockService testLockService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public RedissonClient redissonClient(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port
        ) {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://" + host + ":" + port);
            return Redisson.create(config);
        }

        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
        }

        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Component
    static class TestLockService {
        private final AtomicInteger concurrentCount = new AtomicInteger(0); // 현재 동시에 실행중인 스레드 수를 추적
        private final AtomicInteger maxConcurrent = new AtomicInteger(0); // 동일 실행 스레드 수의 최댓값을 기록

        @DistributedLock(key = "'test:lock:' + #key", waitTime = 5, timeUnit = TimeUnit.SECONDS) // 분산락이 정상 동작하면 한 번에 하나의 스레드만 메서드 진입 가능
        public void executeWithLock(String key) throws InterruptedException {
            int current = concurrentCount.incrementAndGet(); // 진입한 스레드가 concurrentCount를 1 올리고
            maxConcurrent.updateAndGet(max -> Math.max(max, current)); // maxConcurrent 갱신
            try{
                Thread.sleep(100); // 100ms 동안 작업(했다 치고)
            }finally {
                concurrentCount.decrementAndGet(); // concurrentCount 1 내린다 -> 락 해제 -> 다음 스레드 진입
            }
        }

        public int getMaxConcurrent() {
            return maxConcurrent.get();
        }
    }

    @Test
    void 동일_키로_동시_요청시_하나만_실행된다() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount); // CountDownLatch : 카운터가 0이 될 때까지 메인 스레드를 기다리게하는 도구

        List<Throwable> failures = new java.util.concurrent.CopyOnWriteArrayList<>();

        try{
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> { // 스레드 제출 -> 각 스레드는 백그라운드에서 실행, 메인스레드는 스레드 제출만 하고 다음 줄로 넘어감 : executor.submit(()->{}) 5번 실행 후 latch.await()
                    try {
                        testLockService.executeWithLock("same-key"); // 5개의 스레드가 동시에 executeWithLock("same-key") 호출
                    } catch (Exception e) {
                        failures.add(e);
                    } finally {
                        latch.countDown(); // 각 스레드가 락 해제 후 카운터가 1 감소 : 0이 될때까지 메인 스레드가 각 스레드들을 대기
                    }
                });
            }
            boolean result = latch.await(10, TimeUnit.SECONDS);
            assertThat(result).isTrue(); // latch가 0이 될때까지 최대 10초간 대기
            assertThat(failures).isEmpty();
            assertThat(testLockService.getMaxConcurrent()).isEqualTo(1);
        }finally {
            executor.shutdown(); // 스레드 풀 종료 함수 : 사용한 스레드 풀 정리
        }
    }
    // 메인 스레드 종료
}
