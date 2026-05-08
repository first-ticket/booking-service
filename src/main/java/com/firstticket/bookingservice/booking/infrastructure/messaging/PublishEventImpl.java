package com.firstticket.bookingservice.booking.infrastructure.messaging;

import com.firstticket.bookingservice.booking.domain.service.PublishEvent;
import com.firstticket.bookingservice.booking.infrastructure.messaging.payload.BookingPaymentRefundPayload;
import com.firstticket.bookingservice.booking.infrastructure.messaging.payload.CancelSeatListPayload;
import com.firstticket.bookingservice.booking.infrastructure.messaging.payload.PaymentCancelRequestPayload;
import com.firstticket.common.messaging.event.Events;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PublishEventImpl implements PublishEvent {
    @Override
    public void paymentRefundEvent(UUID paymentId, UUID userId, UUID bookingId) {
        Events.publish(
            UUID.randomUUID().toString(),
            "BOOKING",
            bookingId,
            "booking.expired",
            BookingPaymentRefundPayload.of(
                paymentId,
                userId,
                bookingId,
                "좌석 선점 시간 만료"
            )
        );
    }

    //booking.cancel.request
    @Override
    public void cancelBooking(UUID paymentId, UUID userId, UUID bookingId) {
        Events.publish(
            UUID.randomUUID().toString(),
            "BOOKING",
            bookingId,
            "booking.cancel.requested",
            PaymentCancelRequestPayload.of(
                paymentId,
                userId,
                bookingId,
                "사용자 예매 취소"
            )
        );
    }

    //booking.cancel.confirmed
    @Override
    public void cancelBookingConfirmed(List<UUID> seatList, UUID bookingId) {
        Events.publish(
            UUID.randomUUID().toString(),
            "BOOKING",
            bookingId,
            "booking.cancel.confirmed",
            CancelSeatListPayload.of(
                seatList
            )
        );
    }

}
