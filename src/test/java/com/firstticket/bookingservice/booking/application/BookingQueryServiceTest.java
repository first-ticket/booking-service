package com.firstticket.bookingservice.booking.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.firstticket.bookingservice.booking.application.dto.result.BookingDetailResult;
import com.firstticket.bookingservice.booking.application.dto.result.BookingSummaryResult;
import com.firstticket.bookingservice.booking.domain.Booking;
import com.firstticket.bookingservice.booking.domain.BookingRepository;
import com.firstticket.bookingservice.booking.domain.BookingStatus;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import com.firstticket.bookingservice.booking.domain.query.BookingPage;
import com.firstticket.bookingservice.booking.domain.query.BookingPageRequest;
import com.firstticket.bookingservice.booking.domain.query.BookingQueryRepository;
import com.firstticket.bookingservice.booking.domain.query.BookingSearchSpec;
import com.firstticket.bookingservice.booking.domain.query.BookingSummaryData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
            .isInstanceOf(BookingException.class);
    }

    @Test
    void 다른_userId로_조회시_권한_예외가_발생한다() {
        UUID anotherUserId = UUID.randomUUID();
        given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingQueryService.getBookingDetail(anotherUserId, bookingId))
            .isInstanceOf(BookingException.class);
    }

    @Test
    void 정상_조회시_BookingDetailResult가_반환된다() {
        given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));

        BookingDetailResult result = bookingQueryService.getBookingDetail(userId, bookingId);

        assertThat(result).isNotNull();
        assertThat(result.programTitle()).isEqualTo("테스트 공연");
        assertThat(result.venueName()).isEqualTo("올림픽공원");
    }

    @Test
    void 유효하지_않은_status_문자열이면_예외가_발생한다() {
        assertThatThrownBy(() -> bookingQueryService.searchMyBookings(
            userId, "INVALID_STATUS", null, null, 0, 10))
            .isInstanceOf(BookingException.class);
    }

    @Test
    void 정상_다건_조회시_BookingPage가_반환된다() {
        BookingSearchSpec spec = new BookingSearchSpec(userId, null, null, null);
        BookingPageRequest pageRequest = new BookingPageRequest(0, 10);

        BookingPage<BookingSummaryData> dataPage = new BookingPage<>(
            List.of(new BookingSummaryData(
                UUID.randomUUID(),
                "테스트 공연",
                BookingStatus.CONFIRMED,
                10000L,
                1,
                LocalDateTime.now()
            )),
            0, 10, 1L
        );

        given(bookingQueryRepository.search(any(), any())).willReturn(dataPage);

        BookingPage<BookingSummaryResult> result = bookingQueryService.searchMyBookings(
            userId, null, null, null, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content().get(0).programTitle()).isEqualTo("테스트 공연");
    }

    @Test
    void status_필터_적용시_해당_상태로_조회된다() {
        BookingPage<BookingSummaryData> dataPage = new BookingPage<>(
            List.of(new BookingSummaryData(
                UUID.randomUUID(),
                "테스트 공연",
                BookingStatus.CONFIRMED,
                10000L,
                1,
                LocalDateTime.now()
            )),
            0, 10, 1L
        );

        given(bookingQueryRepository.search(any(), any())).willReturn(dataPage);

        BookingPage<BookingSummaryResult> result = bookingQueryService.searchMyBookings(
            userId, "CONFIRMED", null, null, 0, 10);

        ArgumentCaptor<BookingSearchSpec> specCaptor = ArgumentCaptor.forClass(BookingSearchSpec.class);
        then(bookingQueryRepository).should().search(specCaptor.capture(), any());
        assertThat(specCaptor.getValue().status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(result.content().get(0).status()).isEqualTo(BookingStatus.CONFIRMED);
    }
}
