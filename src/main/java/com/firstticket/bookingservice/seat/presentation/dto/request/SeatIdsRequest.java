package com.firstticket.bookingservice.seat.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record SeatIdsRequest(
    @NotEmpty
    List<UUID> seatIds
) {}
