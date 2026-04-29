package com.firstticket.bookingservice.booking.domain.lock;

import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction; // 트랜잭션 분리를 위한 보조 클래스

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock ) throws Throwable{

        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 메서드의 세부정보 : 메서드 이름은 ~이고, 리턴 타입은 ~이고, 첫번째 파라미터 이름은 뭐고 값은 뭐고, 두번째 파라미터 ...

        //1. SpEL을 이용한 키 추출
        String key = CustomSpringElParser.getDynamicValue(
            signature.getParameterNames(),
            joinPoint.getArgs(),
            distributedLock.key()
        );

        //2. Redisson 락 획득 시도
        // 2. Redisson 락 획득 시도
        RLock rLock = redissonClient.getLock(key);

        try {
            // waitTime만큼 기다리고, leaseTime만큼 점유함
            boolean available = rLock.tryLock(
                distributedLock.waitTime(),
                distributedLock.leaseTime(),
                distributedLock.timeUnit()
            );

            if (!available) {
                throw new BookingException(BookingErrorCode.BOOKING_LOCK_FAILED);
            }

            // 3. 트랜잭션이 보장된 상태에서 비즈니스 로직 실행
            return aopForTransaction.proceed(joinPoint);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 스레드 인터럽트 상태 복원 + 원본 예외 전달
            throw e;
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }

    }

}

