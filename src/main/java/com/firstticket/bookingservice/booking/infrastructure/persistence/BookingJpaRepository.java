package com.firstticket.bookingservice.booking.infrastructure.persistence;

import com.firstticket.bookingservice.booking.domain.Booking;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookingJpaRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    boolean existsBySessionId(String sessionId);
}
