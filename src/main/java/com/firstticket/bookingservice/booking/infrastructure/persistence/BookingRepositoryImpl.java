package com.firstticket.bookingservice.booking.infrastructure.persistence;

import com.firstticket.bookingservice.booking.domain.Booking;
import com.firstticket.bookingservice.booking.domain.BookingRepository;
import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookingRepositoryImpl implements BookingRepository {

    private final BookingJpaRepository bookingJpaRepository;

    @Override
    public void save(Booking booking) {
        try{
            bookingJpaRepository.save(booking);
        }catch (DataIntegrityViolationException e){
            throw new BookingException(BookingErrorCode.DUPLICATE_BOOKING);
        }
    }

    @Override
    public boolean isDuplicated(String sessionId) {
        return bookingJpaRepository.existsBySessionId(sessionId);
    }

    @Override
    public Optional<Booking> findById(UUID bookingId) {
        return bookingJpaRepository.findById(bookingId);
    }
}
