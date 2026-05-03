package com.firstticket.bookingservice.booking.infrastructure.client.payment_service;

import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;

public class PaymentClientConfig {
    @Bean
    public ErrorDecoder errorDecoder(){return new PaymentClientErrorDecoder();}

    // 결제 요청을 재시도하면 중복 결제가 발생할 수 있기 때문에
    // 타임아웃 : 연결 3초, 응답 5초 설정
    @Bean
    public Request.Options options(){
        return new Request.Options(3000, TimeUnit.MILLISECONDS, 5000, TimeUnit.MILLISECONDS, true);
    }
    // 재시도 금지
    @Bean
    public Retryer retryer(){
        return Retryer.NEVER_RETRY;
    }
}
