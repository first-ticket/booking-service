package com.firstticket.bookingservice.booking.domain.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingQueryRepository {
    Page<BookingSummaryData> search(BookingSearchSpec spec, Pageable pageable);
}
