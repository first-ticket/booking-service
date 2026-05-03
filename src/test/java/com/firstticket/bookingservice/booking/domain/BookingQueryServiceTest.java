package com.firstticket.bookingservice.booking.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.firstticket.bookingservice.booking.application.BookingQueryService;
import com.firstticket.bookingservice.booking.application.dto.result.BookingDetailResult;
import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import com.firstticket.bookingservice.booking.domain.query.BookingQueryRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingQueryServiceTest {

    @InjectMocks
    private BookingQueryService bookingQueryService;

    @Mock
    private BookingRepository bookingRepository;
    @Mock private BookingQueryRepository bookingQueryRepository;

    private UUID userId;
    private UUID bookingId;
    private Booking booking;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        bookingId = UUID.randomUUID();

        booking = Booking.create(
            userId,
            "test-session-id",
            UUID.randomUUID(),
            UUID.randomUUID(),
            "테스트 공연",
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(10).plusHours(2),
            "올림픽공원",
            "서울시 송파구"
        );
    }

    @Test
    void 존재하지_않는_bookingId_조회시_예외가_발생한다() {
        given(bookingRepository.findById(bookingId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bookingQueryService.getBookingDetail(userId, bookingId))
            .isInstanceOfSatisfying(BookingException.class,
                ex -> assertThat(ex.getErrorCode()).isEqualTo(BookingErrorCode.INVALID_BOOKING_ID));
    }

    @Test
    void 다른_userId로_조회시_권한_예외가_발생한다() {
        UUID anotherUserId = UUID.randomUUID();
        given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingQueryService.getBookingDetail(anotherUserId, bookingId))
            .isInstanceOfSatisfying(BookingException.class,
                ex -> assertThat(ex.getErrorCode()).isEqualTo(BookingErrorCode.INVALID_AUTHORIZATION));
    }

    @Test
    void 정상_조회시_BookingDetailResult가_반환된다() {
        given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));

        BookingDetailResult result = bookingQueryService.getBookingDetail(userId, bookingId);

        assertThat(result).isNotNull();
        assertThat(result.programTitle()).isEqualTo("테스트 공연");
        assertThat(result.venueName()).isEqualTo("올림픽공원");
    }
}
