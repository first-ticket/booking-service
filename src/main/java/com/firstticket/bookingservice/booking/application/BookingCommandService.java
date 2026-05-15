package com.firstticket.bookingservice.booking.application;

import com.firstticket.bookingservice.booking.application.dto.command.CreateBookingCommand;
import com.firstticket.bookingservice.booking.application.dto.result.BookingResult;
import com.firstticket.bookingservice.booking.application.lock.DistributedLock;
import com.firstticket.bookingservice.booking.domain.Booking;
import com.firstticket.bookingservice.booking.domain.BookingItem;
import com.firstticket.bookingservice.booking.domain.BookingRepository;
import com.firstticket.bookingservice.booking.domain.BookingStatus;
import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import com.firstticket.bookingservice.booking.domain.service.PaymentOperator;
import com.firstticket.bookingservice.booking.domain.service.ProgramOperator;
import com.firstticket.bookingservice.booking.domain.service.PublishEvent;
import com.firstticket.bookingservice.booking.domain.service.SeatOperator;
import com.firstticket.bookingservice.booking.domain.service.vo.HeldSeatResult;
import com.firstticket.bookingservice.booking.domain.service.vo.PaymentResult;
import com.firstticket.bookingservice.booking.domain.service.vo.ProgramScheduleResult;
import com.firstticket.common.exception.BusinessException;
import com.firstticket.common.web.AuthContext;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingCommandService {

    private final SeatOperator seatOperator;
    private final ProgramOperator programOperator;
    private final BookingRepository bookingRepository;
    private final PaymentOperator paymentOperator;
    private final PublishEvent publishEvent;

    // 1. 예매 요청 동시성 제어
    @DistributedLock(
        key = "'booking:lock:' + #userId + ':' + #command.scheduleId",
        waitTime = 5, // 락을 기다릴 수 있는 시간
        timeUnit = TimeUnit.SECONDS
    )
    @Transactional //락을 걸고 트랜잭션을 시작해야함 (순서 중요)
    public BookingResult create(UUID userId, CreateBookingCommand command, String sessionId) {

        // 순차적 중복 요청 방지 로직
        Optional<Booking> existedBooking = bookingRepository.findBySessionId(sessionId);
        if(existedBooking.isPresent()){
            BookingStatus status = existedBooking.get().getStatus();
            if(status == BookingStatus.PAID){
                throw new BookingException(BookingErrorCode.DUPLICATE_BOOKING);
            } else if(status != BookingStatus.PENDING){
                throw new BookingException(BookingErrorCode.ALREADY_BOOKED_SESSION);
            } else {
                bookingRepository.hardDelete(existedBooking.get());
            }
        }

        // 좌석 서비스 호출 : 좌석 선점 체크 + 가격 정보
        seatOperator.validateHold(command.seatList(), userId, sessionId); //예외처리 : seat domain에서 예외가 먼저 처리되기 때문에 여기엔 에러가 안옴
        List<HeldSeatResult> heldSeatResults = seatOperator.getHeldSeats(command.scheduleId(), sessionId); // 좌석 도메인에서 예외처리

        // 프로그램 서비스 feign client 호출 : 프로그램 정보 정합성 체크 , 예외처리는 해당 ErrorDecoder에서 담당
        ProgramScheduleResult programScheduleResult = programOperator.validateSchedule(command.scheduleId());

        // 예매 생성
        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(programScheduleResult.saleStartAt()) || now.isAfter(programScheduleResult.saleEndAt())){
            throw new BookingException(BookingErrorCode.INVALID_SCHEDULE_PERIOD);
        }
        Booking booking = Booking.create(
            userId,
            sessionId,
            command.programId(),
            command.scheduleId(),
            programScheduleResult.programTitle(),
            programScheduleResult.eventStartAt(),
            programScheduleResult.eventEndAt(),
            programScheduleResult.venueName(),
            programScheduleResult.venueAddress(),
            programScheduleResult.saleStartAt(),
            programScheduleResult.saleEndAt()
        );
        for(HeldSeatResult s : heldSeatResults){
            booking.addItem(
                s.seatId(),
                s.seatInfo(),
                s.price()
            );
        }

        bookingRepository.save(booking);

        // 결제 서비스 결제 요청 feign client 호출
        PaymentResult paymentResult = paymentOperator.createPayment(booking.getId(), userId, booking.getTotalPrice().getAmount());

        // 예매 정보 반환
        return new BookingResult(
            booking.getId(),
            paymentResult.paymentId(),
            paymentResult.orderId(),
            booking.getProgramTitle(),
            booking.getEventStartAt(),
            booking.getEventEndAt(),
            paymentResult.amount(),
            booking.getTotalCount()
        );
    }

    /*
    결제 완료 : 좌석 선점 확인 -> 선점 유지시 CONFIRMED, 선점 만료시 CANCELED 이후 결제 환불 로직
    * */
    @Transactional
    public void paymentCompleted(UUID bookingId, UUID paymentId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingException(BookingErrorCode.INVALID_BOOKING_ID));

        booking.paid();
        booking.setPaymentId(paymentId); //paymentId 비로소 저장 : 이전까지는 null

        // 결제 완료 후 아직 좌석 선점 유효한지 검증
        List<UUID> seatIds = booking.getBookingItems()
            .stream()
            .map(BookingItem::getSeatId)
            .toList();

        try{
            seatOperator.reserveSeat(
                seatIds,
                booking.getScheduleId(),
                booking.getUserId(),
                booking.getSessionId()
            );
            booking.confirm();
        }catch (BusinessException e){
            booking.cancelReq();
            publishEvent.paymentRefundEvent(paymentId, booking.getUserId(), bookingId);
        }
    }

    @Transactional // 결제 실패
    public void paymentFailed(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingException(BookingErrorCode.INVALID_BOOKING_ID));
        booking.cancel();
    }

    @Transactional // 예매 취소
    public void bookingCancel(@NotNull UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingException(BookingErrorCode.INVALID_BOOKING_ID));
        if(!booking.getUserId().equals(AuthContext.getUserId())){
            throw new BookingException(BookingErrorCode.INVALID_AUTHORIZATION);
        }
        //일단 '예매 취소 요청 상태' : 결제로부터 '취소 확정' 또는 '환불 확정' 메세지 받으면 '예매 취소 확정 상태'로 변경
        booking.cancelReq();

        //예매 취소 이벤트 발행
        publishEvent.cancelBooking(booking.getPaymentId(), booking.getUserId(), booking.getId());
    }

    @Transactional // 환불 확정
    public void refundComplete(@NotNull UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingException(BookingErrorCode.INVALID_BOOKING_ID));
        List<UUID> seatList = booking.getSeatList();
        publishEvent.cancelBookingConfirmed(seatList, bookingId, booking.getScheduleId());

        booking.cancel();
    }
}
