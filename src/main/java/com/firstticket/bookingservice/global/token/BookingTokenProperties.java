package com.firstticket.bookingservice.global.token;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "booking.token")
@Setter
@Getter
@Validated
public class BookingTokenProperties {

    @NotBlank
    private String entrySecret;
    @NotBlank
    private String sessionSecret;
    @NotNull
    private Duration sessionTokenValidity;

}
