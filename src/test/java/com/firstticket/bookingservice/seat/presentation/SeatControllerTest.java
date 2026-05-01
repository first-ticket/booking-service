package com.firstticket.bookingservice.seat.presentation;

import com.firstticket.bookingservice.global.token.BookingTokenClaims;
import com.firstticket.bookingservice.global.token.BookingTokenProvider;
import com.firstticket.bookingservice.seat.application.SeatCommandService;
import com.firstticket.bookingservice.seat.application.SeatQueryService;
import com.firstticket.bookingservice.seat.application.dto.result.HeldSeatItemResult;
import com.firstticket.bookingservice.seat.application.dto.result.SeatResult;
import com.firstticket.bookingservice.seat.domain.exception.SeatErrorCode;
import com.firstticket.bookingservice.seat.domain.exception.SeatException;
import com.firstticket.bookingservice.seat.domain.service.SeatManager;
import com.firstticket.bookingservice.support.RestDocsSupport;
import com.firstticket.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeatController.class)
@Import(GlobalExceptionHandler.class)
class SeatControllerTest extends RestDocsSupport {

    @MockitoBean
    private SeatCommandService seatCommandService;

    @MockitoBean
    private SeatQueryService seatQueryService;

    @MockitoBean
    private SeatManager seatManager;

    @MockitoBean
    private BookingTokenProvider bookingTokenProvider;

    private final UUID scheduleId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("좌석 목록 조회 성공")
    void getSeatList_success() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        UUID seatId1 = UUID.randomUUID();
        UUID seatId2 = UUID.randomUUID();

        SeatResult mockResult = SeatResult.of(
            scheduleId,
            2,
            List.of(
                new SeatResult.SectionItem(
                    "A구역",
                    2,
                    List.of(
                        new SeatResult.SeatedItem(seatId1, 1, 1, 50000, "AVAILABLE"),
                        new SeatResult.StandingItem(seatId2, 1, 50000, "HELD")
                    )
                )
            )
        );

        given(seatQueryService.getSeatList(any(UUID.class)))
            .willReturn(mockResult);

        mockMvc.perform(RestDocumentationRequestBuilders
                .get("/api/v1/seats/schedules/{scheduleId}", scheduleId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("SEAT_LIST_OK"))
            .andDo(document("seat-list-success",
                pathParameters(
                    parameterWithName("scheduleId").description("회차 ID")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("timestamp").description("응답 시간"),
                    fieldWithPath("data.scheduleId").description("회차 ID"),
                    fieldWithPath("data.remainingCount").description("전체 잔여 좌석 수"),
                    fieldWithPath("data.sections[].sectionName").description("구역명"),
                    fieldWithPath("data.sections[].remainingCount").description("구역별 잔여 좌석 수"),
                    fieldWithPath("data.sections[].seats[].seatId").description("좌석 ID"),
                    fieldWithPath("data.sections[].seats[].price").description("좌석 가격"),
                    fieldWithPath("data.sections[].seats[].status").description("좌석 상태 (AVAILABLE, HELD, RESERVED)"),
                    fieldWithPath("data.sections[].seats[].rowNum").description("행 번호 (지정석만)").optional(),
                    fieldWithPath("data.sections[].seats[].colNum").description("열 번호 (지정석만)").optional(),
                    fieldWithPath("data.sections[].seats[].entryNum").description("입장 번호 (스탠딩석만)").optional()
                )
            ));
    }

    @Test
    @DisplayName("존재하지 않는 scheduleId로 조회 시 404 반환")
    void getSeatList_notFound() throws Exception {
        given(seatQueryService.getSeatList(any(UUID.class)))
            .willThrow(new SeatException(SeatErrorCode.SEAT_NOT_FOUND));

        mockMvc.perform(RestDocumentationRequestBuilders
                .get("/api/v1/seats/schedules/{scheduleId}", UUID.randomUUID()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andDo(document("seat-list-not-found",
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("에러 코드"),
                    fieldWithPath("message").description("에러 메시지"),
                    fieldWithPath("timestamp").description("응답 시간")
                )
            ));
    }

    @Test
    @DisplayName("선점 중인 좌석 목록 조회 성공")
    void getHeldSeatList_success() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        List<HeldSeatItemResult> mockResult = List.of(
            new HeldSeatItemResult(UUID.randomUUID(), "A구역 1행 1열", 50000),
            new HeldSeatItemResult(UUID.randomUUID(), "B구역 1번", 30000)
        );

        given(seatQueryService.getHeldSeats(any(UUID.class), any(String.class)))
            .willReturn(mockResult);

        given(bookingTokenProvider.validateSessionToken(any(String.class)))
            .willReturn(new BookingTokenClaims(userId, UUID.randomUUID(), new Date()));

        mockMvc.perform(RestDocumentationRequestBuilders
                .get("/api/v1/seats/schedules/{scheduleId}/hold", scheduleId)
                .header("X-User-Id", userId)
                .header("X-User-Role", "CUSTOMER")
                .header("Authorization", "Bearer test-access-token")
                .header("Booking-Session-Token", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("SEAT_HELD_LIST_OK"))
            .andDo(document("seat-held-list-success",
                pathParameters(
                    parameterWithName("scheduleId").description("회차 ID")
                ),
                requestHeaders(
                    headerWithName("Authorization").description("JWT access token"),
                    headerWithName("Booking-Session-Token").description("예매 세션 토큰")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("timestamp").description("응답 시간"),
                    fieldWithPath("data[].seatId").description("좌석 ID"),
                    fieldWithPath("data[].seatInfo").description("좌석 정보 (예: A구역 1행 1열, B구역 5번)"),
                    fieldWithPath("data[].price").description("좌석 가격")
                )
            ));
    }

    @Test
    @DisplayName("좌석 선점 성공")
    void holdSeats_success() throws Exception {
        willDoNothing()
            .given(seatCommandService)
            .holdSeats(any(), any(UUID.class), any(UUID.class), any(UUID.class), any(String.class));

        given(bookingTokenProvider.validateSessionToken(any(String.class)))
            .willReturn(new BookingTokenClaims(userId, UUID.randomUUID(), new Date()));

        mockMvc.perform(RestDocumentationRequestBuilders
                .post("/api/v1/seats/schedules/{scheduleId}/hold", scheduleId)
                .header("X-User-Id", userId)
                .header("X-User-Role", "CUSTOMER")
                .header("Authorization", "Bearer test-access-token")
                .header("Booking-Session-Token", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "seatIds": ["%s", "%s"]
                        }
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("SEAT_HELD"))
            .andDo(document("seat-hold-success",
                requestHeaders(
                    headerWithName("Authorization").description("JWT access token"),
                    headerWithName("Booking-Session-Token").description("예매 세션 토큰")
                ),
                pathParameters(
                    parameterWithName("scheduleId").description("회차 ID")
                ),
                requestFields(
                    fieldWithPath("seatIds").description("선점할 좌석 ID 목록")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("timestamp").description("응답 시간")
                )
            ));
    }

    @Test
    @DisplayName("이미 선점된 좌석 선점 시도 시 409 반환")
    void holdSeats_alreadyHeld() throws Exception {
        willThrow(new SeatException(SeatErrorCode.SEAT_ALREADY_HELD))
            .given(seatCommandService)
            .holdSeats(any(), any(UUID.class), any(UUID.class), any(UUID.class), any(String.class));

        given(bookingTokenProvider.validateSessionToken(any(String.class)))
            .willReturn(new BookingTokenClaims(userId, UUID.randomUUID(), new Date()));

        mockMvc.perform(RestDocumentationRequestBuilders
                .post("/api/v1/seats/schedules/{scheduleId}/hold", scheduleId)
                .header("X-User-Id", userId)
                .header("X-User-Role", "CUSTOMER")
                .header("Authorization", "Bearer test-access-token")
                .header("Booking-Session-Token", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "seatIds": ["%s"]
                        }
                        """.formatted(UUID.randomUUID())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andDo(document("seat-hold-already-held",
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("에러 코드"),
                    fieldWithPath("message").description("에러 메시지"),
                    fieldWithPath("timestamp").description("응답 시간")
                )
            ));
    }

    @Test
    @DisplayName("좌석 선점 취소 성공")
    void releaseSeats_success() throws Exception {
        willDoNothing()
            .given(seatCommandService)
            .releaseSeats(any(UUID.class), any(UUID.class), any(String.class));

        given(bookingTokenProvider.validateSessionToken(any(String.class)))
            .willReturn(new BookingTokenClaims(userId, UUID.randomUUID(), new Date()));

        mockMvc.perform(RestDocumentationRequestBuilders
                .delete("/api/v1/seats/schedules/{scheduleId}/hold", scheduleId)
                .header("X-User-Id", userId)
                .header("X-User-Role", "CUSTOMER")
                .header("Authorization", "Bearer test-access-token")
                .header("Booking-Session-Token", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("SEAT_RELEASED"))
            .andDo(document("seat-release-success",
                requestHeaders(
                    headerWithName("Authorization").description("JWT access token"),
                    headerWithName("Booking-Session-Token").description("예매 세션 토큰")
                ),
                pathParameters(
                    parameterWithName("scheduleId").description("회차 ID")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("timestamp").description("응답 시간")
                )
            ));
    }
}
