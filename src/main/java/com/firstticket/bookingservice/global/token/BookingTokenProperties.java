package com.firstticket.bookingservice.global.token;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "booking.token")
@Setter
@Getter
public class BookingTokenProperties {

    private String entrySecret;
    private String sessionSecret;
    private Duration sessionTokenValidity;

}
