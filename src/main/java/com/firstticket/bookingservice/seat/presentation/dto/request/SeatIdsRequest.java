package com.firstticket.bookingservice.seat.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SeatIdsRequest(
    `@NotEmpty`
    List<@NotNull UUID> seatIds
) {}
