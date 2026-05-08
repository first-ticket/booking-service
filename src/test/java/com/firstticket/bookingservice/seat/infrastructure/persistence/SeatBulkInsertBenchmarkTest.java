package com.firstticket.bookingservice.seat.infrastructure.persistence;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatRepository;
import com.firstticket.bookingservice.seat.domain.SeatedInfo;
import com.firstticket.bookingservice.seat.domain.Section;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Testcontainers
@Disabled("성능 벤치마크 테스트 - 로컬에서 수동 실행 전용")
class SeatBulkInsertBenchmarkTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withInitScript("sql/schema.sql");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
            () -> postgres.getJdbcUrl() + "?reWriteBatchedInserts=true");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "booking_schema");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatJpaRepository jpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<Seat> makeSeatedSeats(int count) {
        UUID programId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            seats.add(Seat.createSeated(
                programId,
                scheduleId,
                Section.of(UUID.randomUUID(), "A"),
                SeatedInfo.of((i / 10) + 1, (i % 10) + 1),
                50000
            ));
        }
        return seats;
    }

    @Test
    void JPA_saveAll_vs_JDBC_bulkInsert_성능비교() {
        // 워밍업
        jpaRepository.saveAll(makeSeatedSeats(10));
        jdbcTemplate.execute("TRUNCATE TABLE booking_schema.p_seat");
        seatRepository.bulkInsert(makeSeatedSeats(10));
        jdbcTemplate.execute("TRUNCATE TABLE booking_schema.p_seat");

        int[] sizes = {100, 500, 1000, 5000};
        int repeat = 5;

        for (int size : sizes) {
            long jpaTotalTime = 0;
            long jdbcTotalTime = 0;

            System.out.println("=".repeat(55));
            System.out.printf("  SEAT SIZE: %d%n", size);
            System.out.println("=".repeat(55));
            System.out.printf("  %-6s | %-10s | %-10s%n", "회차", "JPA (ms)", "JDBC (ms)");
            System.out.println("-".repeat(55));

            for (int r = 0; r < repeat; r++) {
                List<Seat> jpaSeats = makeSeatedSeats(size);
                long jpaStart = System.currentTimeMillis();
                jpaRepository.saveAll(jpaSeats);
                long jpaTime = System.currentTimeMillis() - jpaStart;
                jpaTotalTime += jpaTime;
                jdbcTemplate.execute("TRUNCATE TABLE booking_schema.p_seat");

                List<Seat> jdbcSeats = makeSeatedSeats(size);
                long jdbcStart = System.currentTimeMillis();
                seatRepository.bulkInsert(jdbcSeats);
                long jdbcTime = System.currentTimeMillis() - jdbcStart;
                jdbcTotalTime += jdbcTime;
                jdbcTemplate.execute("TRUNCATE TABLE booking_schema.p_seat");

                System.out.printf("  %-6d | %-10d | %-10d%n", r + 1, jpaTime, jdbcTime);
            }

            long jpaAvg = jpaTotalTime / repeat;
            long jdbcAvg = jdbcTotalTime / repeat;

            System.out.println("-".repeat(55));
            System.out.printf("  %-6s | %-10d | %-10d%n", "평균", jpaAvg, jdbcAvg);
            System.out.printf("  개선율: %.1f%%%n", (1 - (double) jdbcAvg / jpaAvg) * 100);
            System.out.println("=".repeat(55));
            System.out.println();
        }
    }
}
