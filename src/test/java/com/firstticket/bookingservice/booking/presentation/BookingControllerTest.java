package com.firstticket.bookingservice.booking.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstticket.bookingservice.booking.application.BookingCommandService;
import com.firstticket.bookingservice.booking.application.BookingQueryService;
import com.firstticket.bookingservice.booking.application.dto.result.BookingDetailResult;
import com.firstticket.bookingservice.booking.application.dto.result.BookingResult;
import com.firstticket.bookingservice.booking.application.dto.result.BookingSummaryResult;
import com.firstticket.bookingservice.booking.domain.BookingStatus;
import com.firstticket.bookingservice.booking.domain.query.BookingPage;
import com.firstticket.bookingservice.booking.presentation.dto.request.CreateBookingRequest;
import com.firstticket.bookingservice.global.token.BookingTokenClaims;
import com.firstticket.bookingservice.global.token.BookingTokenProvider;
import com.firstticket.bookingservice.global.token.EntryTokenBlacklistService;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookingController.class)
@AutoConfigureRestDocs
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingCommandService bookingCommandService;

    @MockitoBean
    private BookingQueryService bookingQueryService;

    @MockitoBean
    private BookingTokenProvider tokenProvider;

    @MockitoBean
    private EntryTokenBlacklistService entryTokenBlacklistService;

    private UUID userId;
    private UUID programId;
    private BookingTokenClaims claims;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        programId = UUID.randomUUID();
        claims = new BookingTokenClaims(userId, programId, new Date(System.currentTimeMillis() + 1800000));

        // @BookingToken 해석 시 호출되는 stub
        given(tokenProvider.validateSessionToken(any())).willReturn(claims);
    }

    @Test
    void 예매_생성_성공() throws Exception {
        // given
        UUID scheduleId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();

        CreateBookingRequest request = new CreateBookingRequest(programId, scheduleId, List.of(seatId));

        BookingResult bookingResult = new BookingResult(
            UUID.randomUUID(),
            "order-001",
            "테스트 공연",
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(10).plusHours(2),
            10000L,
            1
        );

        given(bookingCommandService.create(any(), any(), any())).willReturn(bookingResult);

        // when & then
        mockMvc.perform(post("/api/v1/bookings")
                .header("X-User-Id", userId.toString())
                .header("Booking-Session-Token", "Bearer test-session-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andDo(document("booking-create",
                requestHeaders(
                    headerWithName("X-User-Id").description("사용자 ID"),
                    headerWithName("Booking-Session-Token").description("예매 세션 토큰")
                ),
                requestFields(
                    fieldWithPath("programId").description("프로그램 ID"),
                    fieldWithPath("scheduleId").description("스케줄 ID"),
                    fieldWithPath("seatList").description("선택한 좌석 ID 목록")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("timestamp").description("응답 시간"),
                    fieldWithPath("data.paymentId").description("결제 ID"),
                    fieldWithPath("data.orderId").description("주문 ID"),
                    fieldWithPath("data.programTitle").description("공연 제목"),
                    fieldWithPath("data.eventStartAt").description("공연 시작 시간"),
                    fieldWithPath("data.eventEndAt").description("공연 종료 시간"),
                    fieldWithPath("data.totalPrice").description("총 결제 금액"),
                    fieldWithPath("data.totalCount").description("예매 좌석 수")
                )
            ));
    }

    @Test
    void 예매_단건_조회_성공() throws Exception {
        // given
        UUID bookingId = UUID.randomUUID();

        BookingDetailResult result = BookingDetailResult.of(
            "테스트 공연",
            BookingStatus.CONFIRMED,
            10000L,
            1,
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(10).plusHours(2),
            "올림픽공원",
            "서울시 송파구",
            LocalDateTime.now(),
            List.of(new BookingDetailResult.BookingItemInfo("A구역 1열 1번", 10000L))
        );

        given(bookingQueryService.getBookingDetail(any(), eq(bookingId))).willReturn(result);

        // when & then
        mockMvc.perform(get("/api/v1/bookings/my/{bookingId}", bookingId)
                .header("X-User-Id", userId.toString()))
            .andExpect(status().isOk())
            .andDo(document("booking-get-detail",
                pathParameters(
                    parameterWithName("bookingId").description("예매 ID")
                ),
                requestHeaders(
                    headerWithName("X-User-Id").description("사용자 ID")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("timestamp").description("응답 시간"),
                    fieldWithPath("data.programTitle").description("공연 제목"),
                    fieldWithPath("data.status").description("예매 상태"),
                    fieldWithPath("data.totalPrice").description("총 결제 금액"),
                    fieldWithPath("data.totalCount").description("예매 좌석 수"),
                    fieldWithPath("data.eventStartAt").description("공연 시작 시간"),
                    fieldWithPath("data.eventEndAt").description("공연 종료 시간"),
                    fieldWithPath("data.venueName").description("공연 장소"),
                    fieldWithPath("data.venueAddress").description("공연 장소 주소"),
                    fieldWithPath("data.updatedAt").description("최종 상태 변경 시간"),
                    fieldWithPath("data.bookingItemInfos[].seatPosition").description("좌석 위치"),
                    fieldWithPath("data.bookingItemInfos[].price").description("좌석 가격")
                )
            ));
    }

    @Test
    void 예매_다건_조회_성공() throws Exception {
        // given
        BookingSummaryResult summaryResult = BookingSummaryResult.of(
            UUID.randomUUID(),
            "테스트 공연",
            BookingStatus.CONFIRMED,
            10000L,
            1,
            LocalDateTime.now()
        );

        BookingPage<BookingSummaryResult> bookingPage = new BookingPage<>(
            List.of(summaryResult), 0, 10, 1L
        );

        given(bookingQueryService.searchMyBookings(any(), any(), any(), any(), anyInt(), anyInt()))
            .willReturn(bookingPage);

        // when & then
        mockMvc.perform(get("/api/v1/bookings/my")
                .header("X-User-Id", userId.toString())
                .param("page", "0")
                .param("size", "10")
                .param("status", "CONFIRMED"))
            .andExpect(status().isOk())
            .andDo(document("booking-get-list",
                requestHeaders(
                    headerWithName("X-User-Id").description("사용자 ID")
                ),
                queryParameters(
                    parameterWithName("page").description("페이지 번호 (기본값: 0)"),
                    parameterWithName("size").description("페이지 크기 (기본값: 10)"),
                    parameterWithName("status").description("예매 상태 필터 (선택)").optional(),
                    parameterWithName("startDate").description("조회 시작일 (선택)").optional(),
                    parameterWithName("endDate").description("조회 종료일 (선택)").optional()
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("timestamp").description("응답 시간"),
                    fieldWithPath("data.content[].bookingId").description("예매 ID"),
                    fieldWithPath("data.content[].programTitle").description("공연 제목"),
                    fieldWithPath("data.content[].status").description("예매 상태"),
                    fieldWithPath("data.content[].totalPrice").description("총 결제 금액"),
                    fieldWithPath("data.content[].totalCount").description("예매 좌석 수"),
                    fieldWithPath("data.totalElements").description("전체 데이터 수"),
                    fieldWithPath("data.totalPages").description("전체 페이지 수"),
                    fieldWithPath("data.size").description("페이지 크기"),
                    fieldWithPath("data.number").description("현재 페이지 번호"),
                    fieldWithPath("data.first").description("첫 번째 페이지 여부"),
                    fieldWithPath("data.last").description("마지막 페이지 여부"),
                    fieldWithPath("data.empty").description("데이터 없음 여부"),
                    fieldWithPath("data.numberOfElements").description("현재 페이지 데이터 수"),
                    fieldWithPath("data.pageable").description("페이지 요청 정보"),
                    fieldWithPath("data.sort").description("정렬 정보"),
                    fieldWithPath("data.pageable").description("페이지 요청 정보"),
                    fieldWithPath("data.pageable.pageNumber").description("페이지 번호"),
                    fieldWithPath("data.pageable.pageSize").description("페이지 크기"),
                    fieldWithPath("data.pageable.sort").description("정렬 정보"),
                    fieldWithPath("data.pageable.sort.empty").description("정렬 조건 없음 여부"),
                    fieldWithPath("data.pageable.sort.sorted").description("정렬 여부"),
                    fieldWithPath("data.pageable.sort.unsorted").description("미정렬 여부"),
                    fieldWithPath("data.pageable.offset").description("오프셋"),
                    fieldWithPath("data.pageable.paged").description("페이징 여부"),
                    fieldWithPath("data.pageable.unpaged").description("비페이징 여부"),
                    fieldWithPath("data.sort").description("정렬 정보"),
                    fieldWithPath("data.sort.empty").description("정렬 조건 없음 여부"),
                    fieldWithPath("data.sort.sorted").description("정렬 여부"),
                    fieldWithPath("data.sort.unsorted").description("미정렬 여부")
                )
            ));
    }

    @Test
    void 세션_토큰_발급_성공() throws Exception {
        // given
        String entryToken = "test-entry-token";
        BookingTokenClaims entryTokenClaims = new BookingTokenClaims(userId, programId, new Date(System.currentTimeMillis() + 1800000));

        given(tokenProvider.validateEntryToken(entryToken)).willReturn(entryTokenClaims);
        given(entryTokenBlacklistService.tryBlacklist(any(), any())).willReturn(true);
        given(tokenProvider.generateSessionToken(userId, programId)).willReturn("generated-session-token");

        // when & then
        mockMvc.perform(post("/api/v1/bookings/{programId}/session", programId)
                .header("X-User-Id", userId.toString())
                .header("Booking-Entry-Token", "Bearer " + entryToken))
            .andExpect(status().isOk())
            .andDo(document("booking-create-session",
                pathParameters(
                    parameterWithName("programId").description("프로그램 ID")
                ),
                requestHeaders(
                    headerWithName("X-User-Id").description("사용자 ID"),
                    headerWithName("Booking-Entry-Token").description("예매 입장 토큰")
                ),
                responseFields(
                    fieldWithPath("success").description("성공 여부"),
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("timestamp").description("응답 시간"),
                    fieldWithPath("data.sessionToken").description("예매 세션 토큰")
                )
            ));
    }

}
