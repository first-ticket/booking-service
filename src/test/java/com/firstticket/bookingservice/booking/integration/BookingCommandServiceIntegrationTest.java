package com.firstticket.bookingservice.booking.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.firstticket.bookingservice.booking.application.BookingCommandService;
import com.firstticket.bookingservice.booking.application.dto.command.CreateBookingCommand;
import com.firstticket.bookingservice.booking.domain.service.PaymentOperator;
import com.firstticket.bookingservice.booking.domain.service.ProgramOperator;
import com.firstticket.bookingservice.booking.domain.service.SeatOperator;
import com.firstticket.bookingservice.booking.domain.service.dto.HeldSeatResult;
import com.firstticket.bookingservice.booking.domain.service.dto.PaymentResult;
import com.firstticket.bookingservice.booking.domain.service.dto.ProgramScheduleResult;
import com.firstticket.bookingservice.booking.domain.Booking;
import com.firstticket.bookingservice.booking.domain.BookingRepository;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import com.redis.testcontainers.RedisContainer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
class BookingCommandServiceIntegrationTest {

    @Container
    static RedisContainer redis = new RedisContainer(
        DockerImageName.parse("redis:7-alpine")
    );

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @MockitoBean private SeatOperator seatOperator;
    @MockitoBean private ProgramOperator programOperator;
    @MockitoBean private PaymentOperator paymentOperator;
    @MockitoBean private AuditorAware<UUID> auditorAware;

    @TestConfiguration
    static class TestConfig {

        public AuditorAware<UUID> auditorAware() {
            return () -> Optional.of(UUID.randomUUID()); // 고정 UUID 반환
        }
    }

    @Autowired
    private BookingCommandService bookingCommandService;
    @Autowired private BookingRepository bookingRepository;

    private UUID userId;
    private UUID programId;
    private UUID scheduleId;
    private String sessionId;
    private CreateBookingCommand command;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        programId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        sessionId = UUID.randomUUID().toString();

        command = new CreateBookingCommand(programId, scheduleId, List.of(UUID.randomUUID()));

        given(programOperator.validateSchedule(scheduleId)).willReturn(new ProgramScheduleResult(
            "테스트 공연",
            "올림픽공원",
            "서울시 송파구",
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(10).plusHours(2),
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusHours(1)
        ));

        given(seatOperator.getHeldSeats(scheduleId, sessionId)).willReturn(List.of(
            new HeldSeatResult(UUID.randomUUID(), "A구역 1열 1번", 10000L)
        ));

        given(paymentOperator.createPayment(any(), eq(userId), eq(10000L)))
            .willReturn(new PaymentResult(UUID.randomUUID(), "order-001", 10000L));

        given(auditorAware.getCurrentAuditor()).willReturn(Optional.of(userId));
    }

    @Test
    void 예매_생성시_DB에_저장된다() {
        bookingCommandService.create(userId, command, sessionId);

        UUID bookingId = bookingRepository.findIdBySessionId(sessionId);
        Optional<Booking> saved = bookingRepository.findById(bookingId);

        assertThat(saved).isPresent();
        assertThat(saved.get().getProgramTitle()).isEqualTo("테스트 공연");
        assertThat(saved.get().getTotalPrice().getAmount()).isEqualTo(10000L);
    }

    @Test
    void 동일_세션으로_두번_요청시_예외가_발생한다() {
        bookingCommandService.create(userId, command, sessionId);

        assertThatThrownBy(() -> bookingCommandService.create(userId, command, sessionId))
            .isInstanceOf(BookingException.class);
    }
}
