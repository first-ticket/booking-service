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

    @Override
    public Optional<Booking> findBySessionId(String sessionId) {
        return bookingJpaRepository.findBySessionId(sessionId);
    }

    @Override
    public void hardDelete(Booking booking) {
        bookingJpaRepository.delete(booking);
        bookingJpaRepository.flush();
        // DELETE SQL 즉시 실행
        // JPA flush 순서가 INSERT->UPDATE->DELETE 순서로 SQL이 진행된다.
        // DELETE가 실행되기 전에는 두 레코드가 공존하게되므로
        // 이때 sessionId에 걸린 unique 제약에 의해 에러가 생기기 때문에 이를 방지하기 위해 flush를 미리 실행한다.
    }
}
