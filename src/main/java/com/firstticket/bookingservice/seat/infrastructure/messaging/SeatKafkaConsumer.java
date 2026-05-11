package com.firstticket.bookingservice.seat.infrastructure.messaging;

import com.firstticket.bookingservice.seat.application.SeatCommandService;
import com.firstticket.bookingservice.seat.infrastructure.messaging.payload.BookingCancelConfirmedPayload;
import com.firstticket.bookingservice.seat.infrastructure.messaging.payload.ProgramCreatedPayload;
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
public class SeatKafkaConsumer {

    private final SeatCommandService seatCommandService;

    @KafkaListener(topics = "program.created")
    @IdempotentConsumer
    public void consumeProgramCreated(ConsumerRecord<String, String> record, Acknowledgment ack) {
        ProgramCreatedPayload payload = JsonUtil.fromJson(record.value(), ProgramCreatedPayload.class);
        log.info("[SeatKafkaConsumer] 메시지 수신. key={}, value={}", record.key(), payload.toString());

        seatCommandService.createSeats(payload.toCommand());

        ack.acknowledge();
    }

    @KafkaListener(topics = "booking.cancel.confirmed")
    @IdempotentConsumer
    public void consumeBookingCancelConfirmed(ConsumerRecord<String, String> record, Acknowledgment ack) {
        BookingCancelConfirmedPayload payload = JsonUtil.fromJson(record.value(), BookingCancelConfirmedPayload.class);
        log.info("[SeatKafkaConsumer] 메시지 수신. key={}, value={}", record.key(), payload.toString());

        seatCommandService.restoreSeats(payload.seatList());

        ack.acknowledge();
    }
}
