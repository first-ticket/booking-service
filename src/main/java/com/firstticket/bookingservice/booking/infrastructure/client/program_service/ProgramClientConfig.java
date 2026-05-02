package com.firstticket.bookingservice.booking.infrastructure.client.program_service;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/*
    각 FeignClient에서 반환받은 에러코드가 동일할 경우를 대비해 ErrorDecoder을 각각의 FeignClient마다 만들어서 사용한다.
    이때 Config 클래스를 만들어서 Client와 Decoder을 연결한다
* */
public class ProgramClientConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new ProgramClientErrorDecoder();
    }
}
