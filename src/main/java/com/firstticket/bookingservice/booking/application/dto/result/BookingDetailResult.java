package com.firstticket.bookingservice.booking.application.dto.result;

import com.firstticket.bookingservice.booking.domain.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;

public record BookingDetailResult(
    String programTitle,
    BookingStatus status,
    long totalPrice,
    int totalCount,
    LocalDateTime eventStartAt,
    LocalDateTime eventEndAt,
    String venueName,
    String venueAddress,
    LocalDateTime updatedAt,
    List<BookingItemInfo> items
) {
    public static BookingDetailResult of(
        String programTitle,
        BookingStatus status,
        long totalPrice,
        int totalCount,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt,
        String venueName,
        String venueAddress,
        LocalDateTime updatedAt,
        List<BookingItemInfo> items
    ){
        return new BookingDetailResult(
            programTitle,
            status,
            totalPrice,
            totalCount,
            eventStartAt,
            eventEndAt,
            venueName,
            venueAddress,
            updatedAt,
            items
        );
    }


    public record BookingItemInfo(
        String seatPosition,
        long price
    ) {
        public static BookingItemInfo of(String seatPosition, long price){
            return new BookingItemInfo(seatPosition, price);
        }
    }
}
