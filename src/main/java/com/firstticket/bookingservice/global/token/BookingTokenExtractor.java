package com.firstticket.bookingservice.global.token;

public final class BookingTokenExtractor {
    private BookingTokenExtractor() {}

    public static String extract(String sessionToken) {
        return sessionToken.substring(7).trim();
    }
}
