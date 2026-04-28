package com.firstticket.bookingservice.booking.domain;

import com.firstticket.bookingservice.booking.domain.vo.Money;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import com.firstticket.common.persistence.BaseUserEntity;
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

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE) // 외부에서 수정하지 못하도록 메서드를 통해 get 가능 + ( 불변 리스트로 반환할것 )
    private List<BookingItem> bookingItems = new ArrayList<>();

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_price", nullable = false)),
    })
    private Money totalPrice;

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
    }

    private Booking(UUID userId, UUID scheduleId, Long amount ) {
        this.userId = Objects.requireNonNull(userId, "userId는 null일 수 없습니다.");
        this.scheduleId = Objects.requireNonNull(scheduleId, "scheduleId는 null일 수 없습니다.");
        this.status = BookingStatus.PENDING;
        this.totalPrice = new Money(amount);
    }

    public static Booking of(UUID userId, UUID scheduleId, Long amount ){
        return new Booking(userId, scheduleId, amount);
    }

    public void addItem(BookingItem item){
        this.bookingItems.add(item);
    }

    public List<BookingItem> getBookingItems(){
        return Collections.unmodifiableList(this.bookingItems);
    }
}
