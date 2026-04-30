package com.firstticket.bookingservice.seat.application.dto.result;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatStatus;

import java.util.List;
import java.util.UUID;

public record SeatResult(
    UUID scheduleId,
    int remainingCount,
    List<SectionItem> sections
) {
    public static SeatResult of(UUID scheduleId, int remainingCount, List<SectionItem> sections) {
        return new SeatResult(scheduleId, remainingCount, sections);
    }

    public record SectionItem(
        String sectionName,
        int remainingCount,
        List<SeatItem> seats
    ) {}

    public sealed interface SeatItem permits SeatedItem, StandingItem {}

    public record SeatedItem(
        UUID seatId,
        int rowNum,
        int colNum,
        int price,
        String status
    ) implements SeatItem {
        public static SeatedItem from(Seat seat, SeatStatus status) {
            return new SeatedItem(
                seat.getId().id(),
                seat.getSeatedInfo().rowNum(),
                seat.getSeatedInfo().colNum(),
                seat.getPrice(),
                status.name()
            );
        }
    }

    public record StandingItem(
        UUID seatId,
        int entryNum,
        int price,
        String status
    ) implements SeatItem {
        public static StandingItem from(Seat seat, SeatStatus status) {
            return new StandingItem(
                seat.getId().id(),
                seat.getStandingInfo().entryNum(),
                seat.getPrice(),
                status.name()
            );
        }
    }
}
