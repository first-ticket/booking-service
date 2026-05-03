package com.firstticket.bookingservice.booking.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.firstticket.bookingservice.booking.application.dto.command.CreateBookingCommand;
import com.firstticket.bookingservice.booking.application.dto.result.BookingResult;
import com.firstticket.bookingservice.booking.application.port.PaymentOperator;
import com.firstticket.bookingservice.booking.application.port.ProgramOperator;
import com.firstticket.bookingservice.booking.application.port.PublishEvent;
import com.firstticket.bookingservice.booking.application.port.SeatOperator;
import com.firstticket.bookingservice.booking.application.port.dto.HeldSeatResult;
import com.firstticket.bookingservice.booking.application.port.dto.PaymentResult;
import com.firstticket.bookingservice.booking.application.port.dto.ProgramScheduleResult;
import com.firstticket.bookingservice.booking.domain.Booking;
import com.firstticket.bookingservice.booking.domain.BookingRepository;
import com.firstticket.bookingservice.booking.domain.BookingStatus;
import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import com.firstticket.common.messaging.event.Events;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class BookingCommandServiceTest {

    @InjectMocks
    private BookingCommandService bookingCommandService;

    @Mock private SeatOperator seatOperator;
    @Mock private ProgramOperator programOperator;
    @Mock private BookingRepository bookingRepository;
    @Mock private PaymentOperator paymentOperator;
    @Mock private ApplicationEventPublisher applicationEventPublisher;
    @Mock private PublishEvent publishEvent;

    private UUID userId;
    private UUID programId;
    private UUID scheduleId;
    private String sessionId;
    private CreateBookingCommand command;
    private ProgramScheduleResult scheduleResult;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        programId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        sessionId = "test-session-id";

        command = new CreateBookingCommand(programId, scheduleId, List.of(UUID.randomUUID()));

        new Events().init(applicationEventPublisher);

        scheduleResult = new ProgramScheduleResult(
            "테스트 공연",
            "올림픽공원",
            "서울시 송파구",
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(10).plusHours(2),
            LocalDateTime.now().minusHours(1),  // saleStartAt
            LocalDateTime.now().plusHours(1)    // saleEndAt
        );
    }

    @Test
    void 중복_세션이면_예외가_발생한다() {
        given(bookingRepository.isDuplicated(sessionId)).willReturn(true);

        assertThatThrownBy(() -> bookingCommandService.create(userId, command, sessionId))
            .isInstanceOf(BookingException.class);
    }

    @Test
    void 판매기간_외_요청이면_예외가_발생한다() {
        ProgramScheduleResult expiredSchedule = new ProgramScheduleResult(
            "테스트 공연",
            "올림픽공원",
            "서울시 송파구",
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(10).plusHours(2),
            LocalDateTime.now().minusHours(2),  // saleStartAt
            LocalDateTime.now().minusHours(1)   // saleEndAt - 이미 종료
        );

        given(bookingRepository.isDuplicated(sessionId)).willReturn(false);
        given(seatOperator.getHeldSeats(scheduleId, sessionId)).willReturn(List.of(
            new HeldSeatResult(UUID.randomUUID(), "A구역 1열 1번", 10000L)
        ));
        given(programOperator.validateSchedule(scheduleId)).willReturn(expiredSchedule);

        assertThatThrownBy(() -> bookingCommandService.create(userId, command, sessionId))
            .isInstanceOf(BookingException.class);
    }

    @Test
    void 정상_흐름에서_예매가_저장되고_BookingResult가_반환된다() {
        given(bookingRepository.isDuplicated(sessionId)).willReturn(false);
        given(seatOperator.getHeldSeats(scheduleId, sessionId)).willReturn(List.of(
            new HeldSeatResult(UUID.randomUUID(), "A구역 1열 1번", 10000L)
        ));
        given(programOperator.validateSchedule(scheduleId)).willReturn(scheduleResult);
        given(paymentOperator.createPayment(any(), eq(userId), eq(10000L)))
            .willReturn(new PaymentResult(UUID.randomUUID(), "order-001", 10000L));

        BookingResult result = bookingCommandService.create(userId, command, sessionId);

        then(bookingRepository).should().save(any(Booking.class));
        assertThat(result).isNotNull();
        assertThat(result.programTitle()).isEqualTo("테스트 공연");
    }

    @Test
    void paymentCompleted_좌석선점_성공시_CONFIRMED로_전이된다() {
        Booking booking = Booking.create(userId, sessionId, programId, scheduleId,
            "테스트 공연",
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(10).plusHours(2),
            "올림픽공원", "서울시 송파구");

        UUID paymentId = UUID.randomUUID();

        given(bookingRepository.findById(any())).willReturn(Optional.of(booking));

        bookingCommandService.paymentCompleted(booking.getId(), paymentId);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void paymentCompleted_좌석선점_실패시_CANCELED로_전이된다() {
        Booking booking = Booking.create(userId, sessionId, programId, scheduleId,
            "테스트 공연",
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(10).plusHours(2),
            "올림픽공원", "서울시 송파구");

        UUID paymentId = UUID.randomUUID();

        given(bookingRepository.findById(any())).willReturn(Optional.of(booking));
        willThrow(new BookingException(BookingErrorCode.INVALID_SEAT_ID))
            .given(seatOperator).reserveSeat(any(), any(), any(), any());
        bookingCommandService.paymentCompleted(booking.getId(), paymentId);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELED);
    }

    @Test
    void paymentFailed_호출시_CANCELED로_전이된다() {
        Booking booking = Booking.create(userId, sessionId, programId, scheduleId,
            "테스트 공연",
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(10).plusHours(2),
            "올림픽공원", "서울시 송파구");

        given(bookingRepository.findById(any())).willReturn(Optional.of(booking));

        bookingCommandService.paymentFailed(booking.getId());

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELED);
    }
}
