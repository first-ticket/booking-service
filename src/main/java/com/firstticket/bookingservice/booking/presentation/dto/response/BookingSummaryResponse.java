package com.firstticket.bookingservice.booking.presentation.dto.response;

import java.util.UUID;

//예매 다건 조회용
public record BookingSummaryResponse(
    UUID bookingId,
    String programTitle,
    String status,
    long totalPrice,
    int totalCount
) {
    public static BookingSummaryResponse of(UUID bookingId, String programTitle, String status, long totalPrice, int totalCount){
        return new BookingSummaryResponse(bookingId, programTitle, status, totalPrice, totalCount);
    }
}
