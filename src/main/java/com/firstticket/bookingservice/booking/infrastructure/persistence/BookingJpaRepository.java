package com.firstticket.bookingservice.booking.infrastructure.persistence;

import com.firstticket.bookingservice.booking.domain.Booking;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingJpaRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    boolean existsBySessionId(String sessionId);

    @Query("SELECT b.id FROM Booking b WHERE b.sessionId = :sessionId")
    Optional<UUID> findIdBySessionId(@Param("sessionId") String sessionId);
}
