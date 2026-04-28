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

@WebMvcTest(SeatController.class)
@Import(GlobalExceptionHandler.class)
class SeatControllerTest extends RestDocsSupport {

    @MockitoBean
    private SeatCommandService seatCommandService;

    @MockitoBean
    private SeatManager seatManager;

    private final UUID scheduleId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final String sessionId = UUID.randomUUID().toString();

    @Test
    @DisplayName("좌석 선점 성공")
    void holdSeats_success() throws Exception {
        willDoNothing()
            .given(seatCommandService)
            .holdSeats(any(), any(UUID.class), any(String.class));

        mockMvc.perform(RestDocumentationRequestBuilders
                .post("/api/v1/seats/schedules/{scheduleId}/hold", scheduleId)
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
            .andExpect(jsonPath("$.code").value("SEAT_HELD"))
            .andDo(document("seat-hold-success",
                requestHeaders(
                    headerWithName("X-User-Id").description("사용자 ID"),
                    headerWithName("X-Session-Id").description("예매 세션 ID")
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
            .holdSeats(any(), any(UUID.class), any(String.class));

        mockMvc.perform(RestDocumentationRequestBuilders
                .post("/api/v1/seats/schedules/{scheduleId}/hold", scheduleId)
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

        mockMvc.perform(RestDocumentationRequestBuilders
                .delete("/api/v1/seats/schedules/{scheduleId}/hold", scheduleId)
                .header("X-User-Id", userId)
                .header("X-Session-Id", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("SEAT_RELEASED"))
            .andDo(document("seat-release-success",
                requestHeaders(
                    headerWithName("X-User-Id").description("사용자 ID"),
                    headerWithName("X-Session-Id").description("예매 세션 ID")
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
