package com.firstticket.bookingservice.booking.presentation.dto.response;

public record SessionResponse(
    String sessionToken
) {
    public static SessionResponse of(String token){
        return new SessionResponse(token);
    }
}
