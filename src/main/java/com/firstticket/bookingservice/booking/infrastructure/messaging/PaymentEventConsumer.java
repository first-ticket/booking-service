package com.firstticket.bookingservice.booking.infrastructure.messaging;

import com.firstticket.bookingservice.booking.application.BookingCommandService;
import com.firstticket.bookingservice.booking.infrastructure.messaging.payload.PaymentCompletedPayload;
import com.firstticket.bookingservice.booking.infrastructure.messaging.payload.PaymentFailedPayload;
import com.firstticket.bookingservice.booking.infrastructure.messaging.payload.paymentRefundCompletedPayload;
import com.firstticket.common.json.JsonUtil;
import com.firstticket.common.messaging.annotation.IdempotentConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final BookingCommandService bookingCommandService;

    @KafkaListener(topics = "${kafka.topics.payment-completed}")
    @IdempotentConsumer
    public void consumePaymentCompleted(ConsumerRecord<String, String> record, Acknowledgment ack){
        try{
            PaymentCompletedPayload payload = JsonUtil.fromJson(record.value(), PaymentCompletedPayload.class);
            log.info("[결제 성공] 메시지 수신. key={}, value={}", record.key(), payload.toString());

            bookingCommandService.paymentCompleted(payload.bookingId(), payload.paymentId());
        } catch (Exception e){
            log.error("[결제 성공] 메시지 처리 실패. key={}, error={}", record.key(), e.getMessage(), e);
        }finally {
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "${kafka.topics.payment-failed}")
    @IdempotentConsumer
    public void consumePaymentFailed(ConsumerRecord<String, String> record, Acknowledgment ack){
        try{
            PaymentFailedPayload payload = JsonUtil.fromJson(record.value(), PaymentFailedPayload.class);
            log.info("[결제 실패] 메시지 수신. key={}, value={}", record.key(), payload.toString());

            bookingCommandService.paymentFailed(payload.bookingId());
        } catch (Exception e){
            log.error("[결제 실패] 메시지 처리 실패. key={}, error={}", record.key(), e.getMessage(), e);
        }finally {
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "${kafka.topics.payment-refund-completed}")
    @IdempotentConsumer
    public void consumeRefundCompleted(ConsumerRecord<String, String> record, Acknowledgment ack){
        try{
            paymentRefundCompletedPayload payload = JsonUtil.fromJson(record.value(), paymentRefundCompletedPayload.class);
            bookingCommandService.refundComplete(payload.bookingId());
            log.info("[환불 확정] 메시지 수신. key={}, value={}", record.key(), payload.toString());

        } catch (Exception e){
            log.error("[환불 확정] 메시지 처리 실패. key={}, error={}", record.key(), e.getMessage(), e);
        }finally {
            ack.acknowledge();
        }
    }

}
