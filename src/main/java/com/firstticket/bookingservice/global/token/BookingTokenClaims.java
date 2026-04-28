package com.firstticket.bookingservice.global.token;

import java.util.Date;
import java.util.UUID;

public record BookingTokenClaims(UUID userId, UUID programId, Date expirationAt) {}
