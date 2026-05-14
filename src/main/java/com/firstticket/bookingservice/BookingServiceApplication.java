package com.firstticket.bookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ConfigurationPropertiesScan("com.firstticket.bookingservice.global.token") //ConfigurationProperties 붙어있는 클래스를 자동으로 찾아 빈으로 등록
@EntityScan(basePackages = {
    "com.firstticket.bookingservice",
    "com.firstticket.common.messaging"
})
@EnableJpaRepositories(basePackages = {
    "com.firstticket.bookingservice",
    "com.firstticket.common.messaging"
})
public class BookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }

}
