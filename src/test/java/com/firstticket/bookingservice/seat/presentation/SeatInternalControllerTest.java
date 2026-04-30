package com.firstticket.bookingservice.seat.presentation;

import com.firstticket.bookingservice.global.token.BookingTokenProvider;
import com.firstticket.bookingservice.seat.application.SeatQueryService;
import com.firstticket.bookingservice.seat.application.dto.result.SeatRemainingResult;
import com.firstticket.bookingservice.support.RestDocsSupport;
import com.firstticket.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeatInternalController.class)
@Import(GlobalExceptionHandler.class)
class SeatInternalControllerTest extends RestDocsSupport {

    @MockitoBean
    private SeatQueryService seatQueryService;

    @MockitoBean
    private BookingTokenProvider bookingTokenProvider;

    @Test
    @DisplayName("회차별 잔여 좌석 수 조회 성공")
    void getRemainingCounts_success() throws Exception {
        UUID programId = UUID.randomUUID();

        List<SeatRemainingResult> mockResult = List.of(
            new SeatRemainingResult(UUID.randomUUID(), 42),
            new SeatRemainingResult(UUID.randomUUID(), 10)
        );

        given(seatQueryService.getRemainingCounts(any(UUID.class)))
            .willReturn(mockResult);

        mockMvc.perform(RestDocumentationRequestBuilders
                .get("/internal/v1/seats/remaining/{programId}", programId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("SEAT_REMAINING_OK"))
            .andDo(document("seat-remaining-success",
                pathParameters(
                    parameterWithName("programId").description("프로그램 ID")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("timestamp").description("응답 시간"),
                    fieldWithPath("data[].scheduleId").description("회차 ID"),
                    fieldWithPath("data[].remainingCount").description("잔여 좌석 수")
                )
            ));
    }
}
