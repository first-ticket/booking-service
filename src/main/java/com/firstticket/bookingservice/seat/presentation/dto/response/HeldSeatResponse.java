package com.firstticket.bookingservice.seat.presentation.dto.response;

import com.firstticket.bookingservice.seat.application.dto.result.HeldSeatResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public record HeldSeatResponse(
    UUID scheduleId,
    List<SeatItem> seats
) {
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public abstract static class SeatItem {}

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SeatedItem extends SeatItem {
        private final UUID seatId;
        private final String sectionName;
        private final int rowNum;
        private final int colNum;
        private final int price;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StandingItem extends SeatItem {
        private final UUID seatId;
        private final String sectionName;
        private final int entryNum;
        private final int price;
    }

    public static HeldSeatResponse from(HeldSeatResult result) {
        return new HeldSeatResponse(
            result.scheduleId(),
            result.seats().stream()
                .map(seat -> {
                    if (seat instanceof HeldSeatResult.SeatedItem(
                        UUID seatId, String sectionName, int rowNum, int colNum, int price
                    )) {
                        return (SeatItem) new SeatedItem(seatId, sectionName, rowNum, colNum, price);
                    } else if (seat instanceof HeldSeatResult.StandingItem(
                        UUID seatId, String sectionName, int entryNum, int price
                    )) {
                        return (SeatItem) new StandingItem(seatId, sectionName, entryNum, price);
                    }
                    throw new IllegalStateException("알 수 없는 좌석 타입");
                })
                .toList()
        );
    }
}
