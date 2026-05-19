package com.firstticket.bookingservice.booking.infrastructure.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

/**
 * AOP에서 트랜잭션 분리를 위한 클래스
 */
@Component
public class AopForTransaction {

    //@Transactional(propagation = Propagation.REQUIRES_NEW) -> 트랜잭션 범위 축소를 위한 제거
    public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
