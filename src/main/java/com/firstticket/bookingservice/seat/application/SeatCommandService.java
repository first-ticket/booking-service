package com.firstticket.bookingservice.seat.application;

import com.firstticket.bookingservice.seat.application.dto.command.HoldSeatsCommand;
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
    public void holdSeats(HoldSeatsCommand command, UUID userId, String sessionId) {
        List<SeatId> seatIds = command.seatIds().stream()
            .map(SeatId::of)
            .toList();

        List<Seat> seats = seatRepository.findAllByIdInAndScheduleId(seatIds, command.scheduleId());
        if (seats.size() != seatIds.size()) {
            throw new SeatException(SeatErrorCode.SEAT_NOT_FOUND);
        }

        seatManager.holdSeats(seats, command.scheduleId(), userId, sessionId);
    }
}
