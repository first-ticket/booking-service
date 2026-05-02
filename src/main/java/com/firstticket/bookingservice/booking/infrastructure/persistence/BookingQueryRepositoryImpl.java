package com.firstticket.bookingservice.booking.infrastructure.persistence;

import com.firstticket.bookingservice.booking.domain.Booking;
import com.firstticket.bookingservice.booking.domain.QBooking;
import com.firstticket.bookingservice.booking.domain.query.BookingQueryRepository;
import com.firstticket.bookingservice.booking.domain.query.BookingSearchSpec;
import com.firstticket.bookingservice.booking.domain.query.BookingSummaryData;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookingQueryRepositoryImpl implements BookingQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QBooking qBooking = QBooking.booking;

    @Override
    public Page<BookingSummaryData> search(BookingSearchSpec spec, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();

        if (spec.userId() != null) {
            builder.and(qBooking.userId.eq(spec.userId()));
        }
        if (spec.status() != null) {
            builder.and(qBooking.status.eq(spec.status()));
        }
        if (spec.startDate() != null) {
            builder.and(qBooking.eventStartAt.goe(spec.startDate().atStartOfDay()));
        }
        if (spec.endDate() != null) {
            builder.and(qBooking.eventStartAt.loe(spec.endDate().atTime(23, 59, 59)));
        }

        List<Booking> bookings = queryFactory
            .selectFrom(qBooking)
            .where(builder)
            .orderBy(qBooking.updatedAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<BookingSummaryData> content = bookings.stream()
            .map(b -> new BookingSummaryData(
                b.getId(),
                b.getProgramTitle(),
                b.getStatus(),
                b.getTotalPrice().getAmount(),
                b.getTotalCount(),
                b.getUpdatedAt()
            ))
            .toList();

        Long total = queryFactory
            .select(qBooking.count())
            .from(qBooking)
            .where(builder)
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}

