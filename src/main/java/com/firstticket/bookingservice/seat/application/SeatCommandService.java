package com.firstticket.bookingservice.seat.application;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.SeatRepository;
import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import com.firstticket.bookingservice.seat.domain.service.SeatManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatCommandService {

    private final SeatRepository seatRepository;
    private final SeatManager seatManager;

    @Transactional
    public void holdSeats(List<UUID> seatIds, UUID scheduleId, UUID userId, String sessionId) {
        seatManager.holdSeats(getSeats(seatIds, scheduleId), scheduleId, userId, sessionId);
    }

    @Transactional
    public void releaseSeats(UUID scheduleId, UUID userId, String sessionId) {
        List<SeatId> seatIds = seatManager.getHeldSeats(sessionId);
        seatManager.releaseSeats(seatIds, scheduleId, userId, sessionId);
    }

    public void validateHold(List<UUID> seatIds, UUID userId, String sessionId) {
        if (!seatManager.validateHold(toSeatIds(seatIds), userId, sessionId)) {
            throw new SeatException(SeatErrorCode.SEAT_NOT_HELD);
        }
    }

    public void reserveSeats(List<UUID> seatIds, UUID scheduleId, UUID userId, String sessionId) {
        seatManager.reserveSeats(getSeats(seatIds, scheduleId), scheduleId, userId, sessionId);
    }

    private List<Seat> getSeats(List<UUID> seatIds, UUID scheduleId) {
        List<SeatId> ids = toSeatIds(seatIds);

        List<Seat> seats = seatRepository.findAllByIdInAndScheduleId(ids, scheduleId);
        if (seats.size() != ids.size()) {
            throw new SeatException(SeatErrorCode.SEAT_NOT_FOUND);
        }
        return seats;
    }

    private List<SeatId> toSeatIds(List<UUID> seatIds) {
        return seatIds.stream().map(SeatId::of).distinct().toList();
    }

}
