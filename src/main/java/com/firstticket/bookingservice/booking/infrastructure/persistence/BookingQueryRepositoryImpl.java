package com.firstticket.bookingservice.booking.infrastructure.persistence;

import com.firstticket.bookingservice.booking.domain.Booking;
import com.firstticket.bookingservice.booking.domain.QBooking;
import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import com.firstticket.bookingservice.booking.domain.query.BookingPage;
import com.firstticket.bookingservice.booking.domain.query.BookingPageRequest;
import com.firstticket.bookingservice.booking.domain.query.BookingQueryRepository;
import com.firstticket.bookingservice.booking.domain.query.BookingSearchSpec;
import com.firstticket.bookingservice.booking.domain.query.BookingSummaryData;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookingQueryRepositoryImpl implements BookingQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QBooking qBooking = QBooking.booking;

    @Override
    public BookingPage<BookingSummaryData> search(BookingSearchSpec spec, BookingPageRequest pageRequest) {

        if(spec == null){
            throw new IllegalArgumentException("spec must not be null"); // 코드 버그 방어(내부 호출자가 null을 넘기는 경우) : 비즈니스로직 아니므로 자바 표준 예외 적용
        }

        if(pageRequest == null){
            throw new IllegalArgumentException("pageRequest must not be null");
        }

        BooleanBuilder builder = new BooleanBuilder();
        Pageable pageable = PageRequest.of(pageRequest.page(), pageRequest.size());

        if (spec.userId() == null) {
            throw new BookingException(BookingErrorCode.INVALID_USER_ID);
        }
        builder.and(qBooking.userId.eq(spec.userId()));
        if (spec.status() != null) {
            builder.and(qBooking.status.eq(spec.status()));
        }
        if (spec.startDate() != null) {
            builder.and(qBooking.eventStartAt.goe(spec.startDate().atStartOfDay()));
        }
        if (spec.endDate() != null) {
            builder.and(qBooking.eventStartAt.lt(spec.endDate().plusDays(1).atStartOfDay()));
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

        return new BookingPage<>(content, pageRequest.page(), pageRequest.size(), total == null ? 0 : total);
    }
}

