package com.firstticket.bookingservice.seat.infrastructure.event;

import com.firstticket.bookingservice.seat.domain.event.SeatCacheRefreshEvent;
import com.firstticket.bookingservice.seat.infrastructure.redis.SeatCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SeatCacheRefreshHandler {

    private final SeatCacheManager seatCacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SeatCacheRefreshEvent event) {
        seatCacheManager.refreshSeatCache(event.scheduleId());
    }
}
