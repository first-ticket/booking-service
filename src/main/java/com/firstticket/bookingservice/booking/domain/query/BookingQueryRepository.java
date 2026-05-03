package com.firstticket.bookingservice.booking.domain.query;

public interface BookingQueryRepository {
    BookingPage<BookingSummaryData> search(BookingSearchSpec spec, BookingPageRequest pageRequest);
}
