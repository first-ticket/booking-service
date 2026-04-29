package com.firstticket.bookingservice.booking.domain.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    String key();           // SpEL 표현식 (ex. "#userId + ':' + #scheduleId")
    long waitTime();        // 락 획득 대기시간
    long leaseTime();       // 락 보유시간
    TimeUnit timeUnit();    // 시간 단위
}
