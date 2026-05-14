package com.firstticket.bookingservice.global.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
    "com.firstticket.bookingservice",
    "com.firstticket.common.messaging.outbox",
    "com.firstticket.common.messaging.inbox"
})
@EntityScan(basePackages = {
    "com.firstticket.bookingservice",
    "com.firstticket.common.messaging.outbox",
    "com.firstticket.common.messaging.inbox"
})
public class JpaConfig {}
