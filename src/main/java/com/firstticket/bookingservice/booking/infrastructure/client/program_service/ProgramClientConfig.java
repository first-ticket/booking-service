package com.firstticket.bookingservice.booking.infrastructure.client.program_service;

import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import java.util.concurrent.TimeUnit;
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
    // 타임아웃 : 연결 3초, 응답 5초 까지 기다림
    @Bean
    public Request.Options options(){
        return new Request.Options(3000, TimeUnit.MILLISECONDS, 5000, TimeUnit.MILLISECONDS, true);
    }

    //Feign의 기본 재시도 정책 : 지수 백오프 (Exponential Backoff) - 실패가 거듭될수록 대기시간 * 1.5 늘어남 : 서버 부담 줄이기 위한것
    // 재시도 : period ~ MaxPeriod 까지 점진적으로 간격을 증가하며 최대 2회 재시도
    // period(초기 대기 시간) : 첫번째 실패 후, 두번째 시도까지 기다리는 기본 시간
    // maxPeriod(최대 대기 시간) : 점진적으로 증가시키는 대기시간 최대치
    @Bean
    public Retryer retryer(){
        // 기본 대기시간 == 최대 대기 시간 -> 대기시간 고정: 1초 뒤 재시도 한번하고 끝
        return new Retryer.Default(1000, 1000, 2);
    }
}
