package com.firstticket.bookingservice.seat.application.dto.result;

import com.firstticket.bookingservice.seat.domain.Seat;

import java.util.List;
import java.util.UUID;

public record HeldSeatResult(
    UUID scheduleId,
    List<SeatItem> seats
) {
    public static HeldSeatResult of(UUID scheduleId, List<SeatItem> seats) {
        return new HeldSeatResult(scheduleId, seats);
    }

    public sealed interface SeatItem permits SeatedItem, StandingItem {}

    public record SeatedItem(
        UUID seatId,
        String sectionName,
        int rowNum,
        int colNum,
        int price
    ) implements SeatItem {
        public static SeatedItem from(Seat seat) {
            return new SeatedItem(
                seat.getId().id(),
                seat.getSection().sectionName(),
                seat.getSeatedInfo().rowNum(),
                seat.getSeatedInfo().colNum(),
                seat.getPrice()
            );
        }
    }

    public record StandingItem(
        UUID seatId,
        String sectionName,
        int entryNum,
        int price
    ) implements SeatItem {
        public static StandingItem from(Seat seat) {
            return new StandingItem(
                seat.getId().id(),
                seat.getSection().sectionName(),
                seat.getStandingInfo().entryNum(),
                seat.getPrice()
            );
        }
    }
}
