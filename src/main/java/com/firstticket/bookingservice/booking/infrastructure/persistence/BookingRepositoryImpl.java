package com.firstticket.bookingservice.booking.infrastructure.persistence;

import com.firstticket.bookingservice.booking.domain.Booking;
import com.firstticket.bookingservice.booking.domain.BookingRepository;
import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import jakarta.persistence.EntityNotFoundException;
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

    @Override
    public UUID findIdBySessionId(String sessionId) {
        return bookingJpaRepository.findIdBySessionId(sessionId).orElseThrow(() -> new EntityNotFoundException("세션에 해당하는 예약 없음"));
    }
}
