package com.firstticket.bookingservice.booking.application;

import com.firstticket.bookingservice.booking.application.dto.result.BookingDetailResult;
import com.firstticket.bookingservice.booking.application.dto.result.BookingSummaryResult;
import com.firstticket.bookingservice.booking.domain.Booking;
import com.firstticket.bookingservice.booking.domain.BookingRepository;
import com.firstticket.bookingservice.booking.domain.BookingStatus;
import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import com.firstticket.bookingservice.booking.domain.query.BookingQueryRepository;
import com.firstticket.bookingservice.booking.domain.query.BookingSearchSpec;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingQueryService {

    private final BookingRepository bookingRepository;
    private final BookingQueryRepository bookingQueryRepository;

    @Transactional(readOnly = true)
    public Page<BookingSummaryResult> searchMyBookings(UUID userId, String status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        BookingStatus bookingStatus = null;
        if (status != null) {
            try {
                bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BookingException(BookingErrorCode.INVALID_BOOKING_STATUS);
            }
        }

        BookingSearchSpec spec = new BookingSearchSpec(userId, bookingStatus, startDate, endDate);
        return bookingQueryRepository.search(spec, pageable)
            .map(d -> BookingSummaryResult.of(
                d.bookingId(),
                d.programTitle(),
                d.status(),
                d.totalPrice(),
                d.totalCount(),
                d.updatedAt()
            ));
    }

    @Transactional(readOnly = true)
    public BookingDetailResult getBookingDetail(UUID userId, UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingException(BookingErrorCode.INVALID_BOOKING_ID));
        if(!booking.getUserId().equals(userId)){
            throw new BookingException(BookingErrorCode.INVALID_AUTHORIZATION);
        }

        return BookingDetailResult.of(
            booking.getProgramTitle(),
            booking.getStatus(),
            booking.getTotalPrice(),
            booking.getTotalCount(),
            booking.getEventStartAt(),
            booking.getEventEndAt(),
            booking.getVenueName(),
            booking.getVenueAddress(),
            booking.getUpdatedAt(),
            booking.getBookingItems().stream()
            .map(b -> new BookingDetailResult.BookingItemInfo(b.getSeatPosition(), b.getPrice().getAmount()))
            .toList()
        );
    }
}
