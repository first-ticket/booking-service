package com.firstticket.bookingservice.seat.domain.service;

import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatId;
import com.firstticket.bookingservice.seat.domain.SeatedInfo;
import com.firstticket.bookingservice.seat.domain.Section;
import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SeatManagerTest {

    @InjectMocks
    private SeatManager seatManager;

    @Mock
    private SeatHoldManager seatHoldManager;

    private final UUID programId = UUID.randomUUID();
    private final UUID scheduleId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final String sessionId = UUID.randomUUID().toString();
    private final Section section = Section.of(UUID.randomUUID(), "A구역");

    @Test
    @DisplayName("좌석 선점 성공 - 모든 좌석이 AVAILABLE이면 선점 등록")
    void holdSeats_success() {
        Seat seat1 = Seat.createSeated(programId, scheduleId, section, SeatedInfo.of(1, 1), 10000);
        Seat seat2 = Seat.createSeated(programId, scheduleId, section, SeatedInfo.of(1, 2), 10000);
        List<Seat> seats = List.of(seat1, seat2);

        seatManager.holdSeats(seats, programId, scheduleId, userId, sessionId);

        verify(seatHoldManager).hold(
            seats.stream().map(Seat::getId).toList(),
            scheduleId, userId, sessionId
        );
    }

    @Test
    @DisplayName("좌석 선점 실패 - AVAILABLE이 아닌 좌석 존재 시 SEAT_NOT_AVAILABLE 예외 발생")
    void holdSeats_notAvailable() {
        Seat availableSeat = Seat.createSeated(programId, scheduleId, section, SeatedInfo.of(1, 1), 10000);
        Seat reservedSeat = Seat.createSeated(programId, scheduleId, section, SeatedInfo.of(1, 2), 10000);
        reservedSeat.reserve();
        List<Seat> seats = List.of(availableSeat, reservedSeat);

        assertThatThrownBy(() -> seatManager.holdSeats(seats, programId, scheduleId, userId, sessionId))
            .isInstanceOf(SeatException.class)
            .satisfies(e -> assertThat(((SeatException) e).getErrorCode())
                .isEqualTo(SeatErrorCode.SEAT_NOT_AVAILABLE));

        verify(seatHoldManager, never()).hold(anyList(), any(), any(), anyString());
    }

    @Test
    @DisplayName("좌석 선점 실패 - 다른 프로그램의 좌석 선점 시도 시 SEAT_PROGRAM_MISMATCH 예외 발생")
    void holdSeats_programMismatch() {
        UUID otherProgramId = UUID.randomUUID();
        Seat seat = Seat.createSeated(otherProgramId, scheduleId, section, SeatedInfo.of(1, 1), 10000);
        List<Seat> seats = List.of(seat);

        assertThatThrownBy(() -> seatManager.holdSeats(seats, programId, scheduleId, userId, sessionId))
            .isInstanceOf(SeatException.class)
            .satisfies(e -> assertThat(((SeatException) e).getErrorCode())
                .isEqualTo(SeatErrorCode.SEAT_PROGRAM_MISMATCH));

        verify(seatHoldManager, never()).hold(anyList(), any(), any(), anyString());
    }

    @Test
    @DisplayName("예매 확정 성공 - 선점 유효성 확인 후 RESERVED 상태로 변경")
    void reserveSeats_success() {
        Seat seat1 = Seat.createSeated(programId, scheduleId, section, SeatedInfo.of(1, 1), 10000);
        Seat seat2 = Seat.createSeated(programId, scheduleId, section, SeatedInfo.of(1, 2), 10000);
        List<Seat> seats = List.of(seat1, seat2);
        List<SeatId> seatIds = seats.stream().map(Seat::getId).toList();

        given(seatHoldManager.isHeld(seatIds, userId, sessionId)).willReturn(true);
        given(seatHoldManager.releaseAll(seatIds, scheduleId, userId, sessionId)).willReturn(true);

        seatManager.reserveSeats(seats, scheduleId, userId, sessionId);

        assertThat(seat1.isAvailable()).isFalse();
        assertThat(seat2.isAvailable()).isFalse();
        verify(seatHoldManager).releaseAll(seatIds, scheduleId, userId, sessionId);
    }

    @Test
    @DisplayName("예매 확정 실패 - 선점 유효성 확인 실패 시 SEAT_NOT_HELD 예외 발생")
    void reserveSeats_notHeld() {
        Seat seat = Seat.createSeated(programId, scheduleId, section, SeatedInfo.of(1, 1), 10000);
        List<Seat> seats = List.of(seat);
        List<SeatId> seatIds = seats.stream().map(Seat::getId).toList();

        given(seatHoldManager.isHeld(seatIds, userId, sessionId)).willReturn(false);

        assertThatThrownBy(() -> seatManager.reserveSeats(seats, scheduleId, userId, sessionId))
            .isInstanceOf(SeatException.class)
            .satisfies(e -> assertThat(((SeatException) e).getErrorCode())
                .isEqualTo(SeatErrorCode.SEAT_NOT_HELD));

        verify(seatHoldManager, never()).releaseAll(anyList(), any(), any(), anyString());
    }
}
