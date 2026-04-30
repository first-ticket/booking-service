package com.firstticket.bookingservice.seat.application;

import com.firstticket.bookingservice.seat.application.dto.result.SeatRemainingResult;
import com.firstticket.bookingservice.seat.application.dto.result.SeatResult;
import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.SeatRepository;
import com.firstticket.bookingservice.seat.domain.SeatStatus;
import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatQueryService {

    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public SeatResult getSeatList(UUID scheduleId) {
        List<Seat> seats = seatRepository.findByScheduleId(scheduleId);
        if (seats.isEmpty()) {
            throw new SeatException(SeatErrorCode.SEAT_NOT_FOUND);
        }

        Set<SeatId> heldSeatIds = seatRepository.findHeldSeatIds(
            seats.stream().map(Seat::getId).toList()
        );

        int totalRemaining = countAvailable(seats);
        List<SeatResult.SectionItem> sections = groupBySection(seats, heldSeatIds);

        return SeatResult.of(scheduleId, totalRemaining, sections);
    }

    @Transactional(readOnly = true)
    public List<SeatRemainingResult> getRemainingCounts(UUID programId) {
        return seatRepository.countAvailableByProgramId(programId).stream()
            .map(count -> new SeatRemainingResult(count.scheduleId(), (int) count.remainingCount()))
            .toList();
    }

    private int countAvailable(List<Seat> seats) {
        return (int) seats.stream()
            .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
            .count();
    }

    private List<SeatResult.SectionItem> groupBySection(List<Seat> seats, Set<SeatId> heldSeatIds) {
        return seats.stream()
            .collect(Collectors.groupingBy(seat -> seat.getSection().sectionName()))
            .entrySet().stream()
            .map(entry -> toSectionItem(entry.getKey(), entry.getValue(), heldSeatIds))
            .toList();
    }

    private SeatResult.SectionItem toSectionItem(String sectionName, List<Seat> seats, Set<SeatId> heldSeatIds) {
        int remainingCount = countAvailable(seats);
        List<SeatResult.SeatItem> seatItems = seats.stream()
            .map(seat -> toSeatItem(seat, heldSeatIds))
            .toList();
        return new SeatResult.SectionItem(sectionName, remainingCount, seatItems);
    }

    private SeatResult.SeatItem toSeatItem(Seat seat, Set<SeatId> heldSeatIds) {
        SeatStatus status = heldSeatIds.contains(seat.getId())
            ? SeatStatus.HELD : seat.getStatus();

        return switch (seat.getSeatType()) {
            case SEATED -> SeatResult.SeatedItem.from(seat, status);
            case STANDING -> SeatResult.StandingItem.from(seat, status);
        };
    }
}
