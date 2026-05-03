package com.firstticket.bookingservice.booking.domain.query;

import java.util.List;

public record BookingPage<T>(
    List<T> content,
    int page,
    int size,
    long totalElements
) {
    public <R> BookingPage<R> map(java.util.function.Function<T, R> mapper) {
        return new BookingPage<>(
            content.stream().map(mapper).toList(),
            page, size, totalElements
        );
    }
}
