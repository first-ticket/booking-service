package com.firstticket.bookingservice.booking.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;

import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import javax.sql.DataSource;
import com.firstticket.bookingservice.booking.application.lock.DistributedLock;
import com.firstticket.bookingservice.booking.infrastructure.lock.AopForTransaction;
import com.firstticket.bookingservice.booking.infrastructure.lock.CustomSpringElParser;
import com.firstticket.bookingservice.booking.infrastructure.lock.DistributedLockAspect;
import com.redis.testcontainers.RedisContainer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
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
        private final AtomicInteger concurrentCount = new AtomicInteger(0);
        private final AtomicInteger maxConcurrent = new AtomicInteger(0);

        @DistributedLock(key = "'test:lock:' + #key", waitTime = 5, timeUnit = TimeUnit.SECONDS)
        public void executeWithLock(String key) throws InterruptedException {
            int current = concurrentCount.incrementAndGet();
            maxConcurrent.updateAndGet(max -> Math.max(max, current));
            Thread.sleep(100);
            concurrentCount.decrementAndGet();
        }

        public int getMaxConcurrent() {
            return maxConcurrent.get();
        }
    }

    @Test
    void 동일_키로_동시_요청시_하나만_실행된다() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    testLockService.executeWithLock("same-key");
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(testLockService.getMaxConcurrent()).isEqualTo(1);
    }
}
