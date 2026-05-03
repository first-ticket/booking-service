package com.firstticket.bookingservice.booking.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

//예매 단건 조회용
public record BookingResponse(
    String programTitle,
    String status,
    long totalPrice,
    int totalCount,
    LocalDateTime eventStartAt,
    LocalDateTime eventEndAt,
    String venueName,
    String venueAddress,
    LocalDateTime updatedAt,
    List<BookingItemInfo> bookingItemInfos
) {
    public static BookingResponse of(
        String programTitle,
        String status,
        long totalPrice,
        int totalCount,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt,
        String venueName,
        String venueAddress,
        LocalDateTime updatedAt,
        List<BookingItemInfo> bookingItemInfos
    ){
        return new BookingResponse(
            programTitle,
            status,
            totalPrice,
            totalCount,
            eventStartAt,
            eventEndAt,
            venueName,
            venueAddress,
            updatedAt,
            bookingItemInfos
        );
    }
    public record BookingItemInfo(
        String seatPosition,
        long price
    ){
        public static BookingItemInfo of(
            String seatPosition,
            long price
        ){
            return new BookingItemInfo(seatPosition, price);
        }
    }

}
