package com.firstticket.bookingservice.seat.application;

import com.firstticket.bookingservice.seat.application.dto.command.CreateSeatsCommand;
import com.firstticket.bookingservice.seat.domain.Seat;
import com.firstticket.bookingservice.seat.domain.SeatRepository;
import com.firstticket.bookingservice.seat.domain.SeatType;
import com.firstticket.bookingservice.seat.domain.service.SeatManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SeatCommandServiceTest {

    @InjectMocks
    private SeatCommandService seatCommandService;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatManager seatManager;

    @Test
    @DisplayName("SEATED, STANDING 템플릿 기반으로 좌석이 벌크 생성된다")
    void createSeats() {
        CreateSeatsCommand command = new CreateSeatsCommand(
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(
                new CreateSeatsCommand.SeatTemplateCommand(
                    UUID.randomUUID(), "A구역", SeatType.SEATED, 3, 5, null, 50000
                ),
                new CreateSeatsCommand.SeatTemplateCommand(
                    UUID.randomUUID(), "B구역", SeatType.STANDING, null, null, 10, 30000
                )
            )
        );

        seatCommandService.createSeats(command);

        ArgumentCaptor<List<Seat>> captor = ArgumentCaptor.forClass(List.class);
        verify(seatRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(25); // SEATED 3*5 + STANDING 10
    }
}
