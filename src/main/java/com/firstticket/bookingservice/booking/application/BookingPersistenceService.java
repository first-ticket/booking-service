package com.firstticket.bookingservice.booking.application;


import com.firstticket.bookingservice.booking.domain.Booking;
import com.firstticket.bookingservice.booking.domain.BookingRepository;
import com.firstticket.bookingservice.booking.domain.BookingStatus;
import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BookingPersistenceService {

    private final BookingRepository bookingRepository;

    @Transactional  // 짧고 좁은 트랜잭션: save만
    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Transactional  // 중복 체크 + 삭제
    public void checkAndDeleteDuplicate(String sessionId) {
        Optional<Booking> existedBooking = bookingRepository.findBySessionId(sessionId);

        if(existedBooking.isPresent()){
            BookingStatus status = existedBooking.get().getStatus();
            if(status == BookingStatus.PAID){
                throw new BookingException(BookingErrorCode.DUPLICATE_BOOKING);
            } else if(status != BookingStatus.PENDING){
                throw new BookingException(BookingErrorCode.ALREADY_BOOKED_SESSION);
            } else {
                bookingRepository.hardDelete(existedBooking.get());
            }
        }
    }

    @Transactional
    public void deleteBooking(UUID bookingId) {
        bookingRepository.findById(bookingId)
            .ifPresent(bookingRepository::hardDelete);
    }
}
