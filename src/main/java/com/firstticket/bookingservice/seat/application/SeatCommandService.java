package com.firstticket.bookingservice.seat.application;

import com.firstticket.bookingservice.seat.application.dto.command.CreateSeatsCommand;
import com.firstticket.bookingservice.seat.application.dto.command.CreateSeatsCommand.SeatTemplateCommand;
import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.SeatRepository;
import com.firstticket.bookingservice.seat.domain.SeatType;
import com.firstticket.bookingservice.seat.domain.SeatedInfo;
import com.firstticket.bookingservice.seat.domain.Section;
import com.firstticket.bookingservice.seat.domain.StandingInfo;
import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import com.firstticket.bookingservice.seat.domain.service.SeatManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatCommandService {

    private final SeatRepository seatRepository;
    private final SeatManager seatManager;

    @Transactional
    public void createSeats(CreateSeatsCommand command) {
        List<Seat> seats = new ArrayList<>();

        for (SeatTemplateCommand template : command.seatTemplates()) {
            Section section = Section.of(template.sectionId(), template.sectionName());

            if (template.seatType() == SeatType.SEATED) {
                for (int row = 1; row <= template.rowCount(); row++) {
                    for (int col = 1; col <= template.colCount(); col++) {
                        seats.add(Seat.createSeated(
                            command.programId(),
                            command.scheduleId(),
                            section,
                            SeatedInfo.of(row, col),
                            template.price()
                        ));
                    }
                }
            } else if (template.seatType() == SeatType.STANDING) {
                for (int c = 1; c <= template.capacity(); c++) {
                    seats.add(Seat.createStanding(
                        command.programId(),
                        command.scheduleId(),
                        section,
                        StandingInfo.of(c),
                        template.price()
                    ));
                }
            }
        }

        seatRepository.bulkInsert(seats);
    }

    @Transactional
    public void holdSeats(List<UUID> seatIds, UUID programId, UUID scheduleId, UUID userId, String sessionId) {
        seatManager.holdSeats(getSeats(seatIds, scheduleId), programId, scheduleId, userId, sessionId);
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

    @Transactional
    public void reserveSeats(List<UUID> seatIds, UUID scheduleId, UUID userId, String sessionId) {
        seatManager.reserveSeats(getSeats(seatIds, scheduleId), scheduleId, userId, sessionId);
        seatRepository.refreshSeatCache(scheduleId);
    }

    @Transactional
    public void restoreSeats(List<UUID> seatIds, UUID scheduleId) {
        List<Seat> seats = getSeats(seatIds, scheduleId);
        seats.forEach(Seat::restore);
        seatRepository.refreshSeatCache(scheduleId);
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
