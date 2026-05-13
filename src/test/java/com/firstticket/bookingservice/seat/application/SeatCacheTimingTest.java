package com.firstticket.bookingservice.seat.application;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatRepository;
import com.firstticket.bookingservice.seat.domain.SeatStatus;
import com.firstticket.bookingservice.seat.domain.SeatedInfo;
import com.firstticket.bookingservice.seat.domain.Section;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import com.firstticket.bookingservice.seat.infrastructure.persistence.SeatJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@Disabled("좌석 조회 캐시 테스트 - 로컬 캐시 검증용")
class SeatCacheTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");

        // Redis
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private SeatCommandService seatCommandService;

    @Autowired
    private SeatJpaRepository seatJpaRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private CacheManager cacheManager;

    private UUID scheduleId;
    private UUID programId;
    private UUID userId;
    private String sessionId;
    private UUID seatId;

    @BeforeEach
    void setUp() {
        scheduleId = UUID.randomUUID();
        programId = UUID.randomUUID();
        userId = UUID.randomUUID();
        sessionId = UUID.randomUUID().toString();

        Seat seat = Seat.createSeated(
            programId,
            scheduleId,
            Section.of(UUID.randomUUID(), "A구역"),
            SeatedInfo.of(1, 1),
            50000
        );
        seatJpaRepository.save(seat);
        seatId = seat.getId().id();

        // 캐시 초기화
        cacheManager.getCache("seats").clear();
    }

    @Test
    @DisplayName("트랜잭션 롤백 시 캐시가 갱신되지 않는다")
    void 롤백_시_캐시_정합성_검증() {
        // given - 캐시 초기 적재
        seatRepository.findByScheduleId(scheduleId);

        // when - 존재하지 않는 seatId로 reserveSeats 호출 → 예외 발생 → 롤백
        assertThrows(SeatException.class, () ->
            seatCommandService.reserveSeats(
                List.of(UUID.randomUUID()), // 존재하지 않는 seatId
                scheduleId,
                userId,
                sessionId
            )
        );

        // then - 캐시는 갱신되지 않아야 함 (AFTER_COMMIT이므로)
        List<Seat> cached = seatRepository.findByScheduleId(scheduleId);
        assertThat(cached)
            .allMatch(seat -> seat.getStatus() == SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("캐시 히트 시 DB 조회 없이 캐시에서 반환된다")
    void 캐시_히트_검증() {
        // given - 첫 번째 조회로 캐시 적재
        seatRepository.findByScheduleId(scheduleId);

        // DB 비워서 캐시랑 불일치 상태 만들기
        seatJpaRepository.deleteAll();

        // when - 두 번째 조회 (캐시에서 와야 함)
        List<Seat> cached = seatRepository.findByScheduleId(scheduleId);

        // then - DB 비워도 캐시에서 반환되면 캐시 히트 정상
        assertThat(cached).isNotEmpty();
    }
}
