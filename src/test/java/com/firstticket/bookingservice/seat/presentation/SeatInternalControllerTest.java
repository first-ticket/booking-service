package com.firstticket.bookingservice.seat.presentation;

import com.firstticket.bookingservice.seat.application.SeatCommandService;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
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

@WebMvcTest(SeatInternalController.class)
@Import(GlobalExceptionHandler.class)
class SeatInternalControllerTest extends RestDocsSupport {

    @MockitoBean
    private SeatCommandService seatCommandService;

    @MockitoBean
    private SeatManager seatManager;

    private final UUID scheduleId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final String sessionId = UUID.randomUUID().toString();

    @Test
    @DisplayName("선점 유효성 확인 성공")
    void validateHold_success() throws Exception {
        willDoNothing()
            .given(seatCommandService)
            .validateHold(anyList(), any(UUID.class), anyString());

        mockMvc.perform(RestDocumentationRequestBuilders
                .post("/internal/v1/seats/hold/valid")
                .header("X-User-Id", userId)
                .header("X-Session-Id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "seatIds": ["%s", "%s"]
                        }
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andDo(document("seat-hold-valid-success",
                requestHeaders(
                    headerWithName("X-User-Id").description("사용자 ID"),
                    headerWithName("X-Session-Id").description("예매 세션 ID")
                ),
                requestFields(
                    fieldWithPath("seatIds").description("유효성 확인할 좌석 ID 목록")
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
    @DisplayName("선점 유효성 확인 실패 - 선점 정보가 유효하지 않으면 409 반환")
    void validateHold_notHeld() throws Exception {
        willThrow(new SeatException(SeatErrorCode.SEAT_NOT_HELD))
            .given(seatCommandService)
            .validateHold(anyList(), any(UUID.class), anyString());

        mockMvc.perform(RestDocumentationRequestBuilders
                .post("/internal/v1/seats/hold/valid")
                .header("X-User-Id", userId)
                .header("X-Session-Id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "seatIds": ["%s"]
                        }
                        """.formatted(UUID.randomUUID())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andDo(document("seat-hold-valid-failed",
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("에러 코드"),
                    fieldWithPath("message").description("에러 메시지"),
                    fieldWithPath("timestamp").description("응답 시간")
                )
            ));
    }

    @Test
    @DisplayName("좌석 확정 성공")
    void reserveSeats_success() throws Exception {
        willDoNothing()
            .given(seatCommandService)
            .reserveSeats(anyList(), any(UUID.class), any(UUID.class), anyString());

        mockMvc.perform(RestDocumentationRequestBuilders
                .patch("/internal/v1/seats/schedules/{scheduleId}/reserve", scheduleId)
                .header("X-User-Id", userId)
                .header("X-Session-Id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "seatIds": ["%s", "%s"]
                        }
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andDo(document("seat-reserve-success",
                requestHeaders(
                    headerWithName("X-User-Id").description("사용자 ID"),
                    headerWithName("X-Session-Id").description("예매 세션 ID")
                ),
                pathParameters(
                    parameterWithName("scheduleId").description("회차 ID")
                ),
                requestFields(
                    fieldWithPath("seatIds").description("확정할 좌석 ID 목록")
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
    @DisplayName("좌석 확정 실패 - 선점 정보가 유효하지 않으면 409 반환")
    void reserveSeats_notHeld() throws Exception {
        willThrow(new SeatException(SeatErrorCode.SEAT_NOT_HELD))
            .given(seatCommandService)
            .reserveSeats(anyList(), any(UUID.class), any(UUID.class), anyString());

        mockMvc.perform(RestDocumentationRequestBuilders
                .patch("/internal/v1/seats/schedules/{scheduleId}/reserve", scheduleId)
                .header("X-User-Id", userId)
                .header("X-Session-Id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "seatIds": ["%s"]
                        }
                        """.formatted(UUID.randomUUID())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andDo(document("seat-reserve-failed",
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("에러 코드"),
                    fieldWithPath("message").description("에러 메시지"),
                    fieldWithPath("timestamp").description("응답 시간")
                )
            ));
    }
}
