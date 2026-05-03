package com.firstticket.bookingservice.booking.domain;

import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import com.firstticket.bookingservice.booking.domain.vo.Money;
import com.firstticket.common.persistence.BaseUserEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "P_BOOKING")
public class Booking extends BaseUserEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    @Column(name = "program_title", nullable = false)
    private String programTitle;

    @Column(name = "event_start_at", nullable = false)
    private LocalDateTime eventStartAt;

    @Column(name = "event_end_at", nullable = false)
    private LocalDateTime eventEndAt;

    @Column(name = "venue_name", nullable = false)
    private String venueName;

    @Column(name = "venue_address", nullable = false)
    private String venueAddress;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE) // 외부에서 수정하지 못하도록 메서드를 통해 get 가능 + ( 불변 리스트로 반환할것 )
    private List<BookingItem> bookingItems = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_price", nullable = false)),
    })
    private Money totalPrice;

    // Booking(예매)에 포함된 BookingItem(좌석) 총 개수
    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(name = "expired_at") // 만료되어 무효가 된 시점
    private LocalDateTime expiredAt;



    public void paid(){
        this.status = this.status.validateTransition(BookingStatus.PAID);
    }

    public void confirm(){
        this.status =this.status.validateTransition(BookingStatus.CONFIRMED);
    }

    public void cancel(){
        this.status =this.status.validateTransition(BookingStatus.CANCELED);
        this.expiredAt = LocalDateTime.now();
    }

    private Booking(
        UUID userId,
        String sessionId,
        UUID programId,
        UUID scheduleId,
        String programTitle,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt,
        String venueName,
        String venueAddress
    ) {
        this.userId = Objects.requireNonNull(userId, "userId는 null일 수 없습니다.");
        if(sessionId == null || sessionId.isBlank()){
            throw new BookingException(BookingErrorCode.INVALID_SESSION_ID);
        }
        this.sessionId = sessionId;
        this.programId = Objects.requireNonNull(programId, "programId는 null일 수 없습니다.");
        this.scheduleId = Objects.requireNonNull(scheduleId, "scheduleId는 null일 수 없습니다.");
        this.status = BookingStatus.PENDING;
        this.totalPrice = new Money(0L);
        this.totalCount = 0;
        if(programTitle == null || programTitle.isBlank()){
            throw new BookingException(BookingErrorCode.INVALID_PROGRAM_TITLE);
        }

        // 시간 논리 검증 (시작 시간이 종료 시간보다 뒤일 수 없음)
        if (eventStartAt == null || eventEndAt == null || eventStartAt.isAfter(eventEndAt)) {
            throw new BookingException(BookingErrorCode.INVALID_EVENT_TIME);
        }

        // 문자열 공백 및 null 체크
        if (venueName == null || venueName.isBlank()) {
            throw new BookingException(BookingErrorCode.INVALID_VENUE_NAME);
        }

        // 문자열 공백 및 null 체크
        if (venueAddress == null || venueAddress.isBlank()) {
            throw new BookingException(BookingErrorCode.INVALID_VENUE_ADDRESS);
        }

        this.programTitle = programTitle;
        this.eventStartAt = eventStartAt;
        this.eventEndAt = eventEndAt;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
    }

    public static Booking create(
        UUID userId,
        String sessionId,
        UUID programId,
        UUID scheduleId,
        String programTitle,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt,
        String venueName,
        String venueAddress
    ){
        return new Booking(
            userId,
            sessionId,
            programId,
            scheduleId,
            programTitle,
            eventStartAt,
            eventEndAt,
            venueName,
            venueAddress
        );
    }

    //양방향 연관관계 불변식을 위해 aggregate root가 자식 생성/연결을 직접 통제하게함
    public void addItem(
        UUID seatId,
        String seatPosition,
        Long seatPrice
    ){
        Money price = new Money(seatPrice);
        BookingItem item = BookingItem.of(
            this,
            seatId,
            seatPosition,
            price
        );
        this.bookingItems.add(item);
        this.totalCount++;
        this.totalPrice = this.totalPrice.plus(price.getAmount());
    }

    // 외부에서 변경 못하도록 불변 리스트로 반환 (getter 로는 접근 불가)
    public List<BookingItem> getBookingItems(){
        return Collections.unmodifiableList(this.bookingItems);
    }
}
