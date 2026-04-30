package com.firstticket.bookingservice.seat.presentation.dto.response;

import com.firstticket.bookingservice.seat.application.dto.result.SeatResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public record SeatResponse(
    UUID scheduleId,
    int remainingCount,
    List<SectionItem> sections
) {
    public record SectionItem(
        String sectionName,
        int remainingCount,
        List<SeatItem> seats
    ) {}

    public abstract static class SeatItem {}

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SeatedItem extends SeatItem {
        private final UUID seatId;
        private final int rowNum;
        private final int colNum;
        private final int price;
        private final String status;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StandingItem extends SeatItem {
        private final UUID seatId;
        private final int entryNum;
        private final int price;
        private final String status;
    }

    public static SeatResponse from(SeatResult result) {
        return new SeatResponse(
            result.scheduleId(),
            result.remainingCount(),
            result.sections().stream()
                .map(section -> new SectionItem(
                    section.sectionName(),
                    section.remainingCount(),
                    section.seats().stream()
                        .map(seat -> {
                            if (seat instanceof SeatResult.SeatedItem(
                                UUID seatId, int rowNum, int colNum, int price, String status
                            )) {
                                return new SeatedItem(seatId, rowNum, colNum, price, status);
                            }
                            else if (seat instanceof SeatResult.StandingItem(
                                UUID seatId, int entryNum, int price, String status
                            )) {
                                return new StandingItem(seatId, entryNum, price, status);
                            }
                            throw new IllegalStateException("알 수 없는 좌석 타입");
                        })
                        .toList()
                ))
                .toList()
        );
    }
}
