package com.firstticket.bookingservice.booking.presentation;

import com.firstticket.bookingservice.booking.application.BookingCommandService;
import com.firstticket.bookingservice.booking.application.BookingQueryService;
import com.firstticket.bookingservice.booking.application.dto.result.BookingDetailResult;
import com.firstticket.bookingservice.booking.application.dto.result.BookingResult;
import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import com.firstticket.bookingservice.booking.presentation.dto.request.CreateBookingRequest;
import com.firstticket.bookingservice.booking.presentation.dto.response.BookingResponse;
import com.firstticket.bookingservice.booking.presentation.dto.response.BookingSummaryResponse;
import com.firstticket.bookingservice.booking.presentation.dto.response.SessionResponse;
import com.firstticket.bookingservice.global.annotation.BookingToken;
import com.firstticket.bookingservice.global.token.BookingTokenClaims;
import com.firstticket.bookingservice.global.token.BookingTokenProvider;
import com.firstticket.bookingservice.global.token.EntryTokenBlacklistService;
import com.firstticket.common.response.ApiResponse;
import com.firstticket.common.response.CommonSuccessCode;
import com.firstticket.common.web.AuthContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingCommandService bookingCommandService;
    private final BookingQueryService bookingQueryService;
    private final BookingTokenProvider tokenProvider;
    private final EntryTokenBlacklistService entryTokenBlacklistService;

    //예매 생성
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResult>> create(
        @RequestBody @Valid CreateBookingRequest request,
        @BookingToken BookingTokenClaims claims,
        @RequestHeader("Booking-Session-Token") String headerSession
    ){
        if(!request.programId().equals(claims.programId())){
            throw new BookingException(BookingErrorCode.INVALID_PROGRAM_ID);
        }
        String token = headerSession.substring(7).trim();
        BookingResult bookingResult = bookingCommandService.create(AuthContext.getUserId(), request.toCommand(), token);

        return ApiResponse.success(CommonSuccessCode.CREATED, bookingResult);
    }


    //본인 예매 다건 조회
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<BookingSummaryResponse>>> getMyBookings(
        Pageable pageable,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate
    ){
        UUID userId = AuthContext.getUserId();
        Page<BookingSummaryResponse> response = bookingQueryService.searchMyBookings(
            userId,
            status,
            startDate,
            endDate,
            pageable
        ).map(r -> BookingSummaryResponse.of(
                r.bookingId(),
                r.programTitle(),
                r.status().name(),
                r.totalPrice(),
                r.totalCount()
            ));

        return ApiResponse.success(CommonSuccessCode.OK, response);
    }


    //본인 예매 단건 조회
    @GetMapping("/my/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getMyBookingDetail(
        @PathVariable("bookingId") UUID bookingId
    ){
        UUID userId = AuthContext.getUserId();
        BookingDetailResult result = bookingQueryService.getBookingDetail(userId, bookingId);

        return ApiResponse.success(
            CommonSuccessCode.OK,
            BookingResponse.of(
                result.programTitle(),
                result.status().toString(),
                result.totalPrice().getAmount(),
                result.totalCount(),
                result.eventStartAt(),
                result.eventEndAt(),
                result.venueName(),
                result.venueAddress(),
                result.updatedAt(),
                result.items()
                    .stream()
                    .map(b -> BookingResponse.BookingItemInfo.of(b.seatPosition(), b.price()))
                    .toList()
        ));
    }

    // 세션 토큰 발급
    @PostMapping("/{programId}/session")
    public ResponseEntity<ApiResponse<SessionResponse>> createBookingSession(
        @RequestHeader("Booking-Entry-Token") String entryHeader,
        @NotNull @PathVariable(name = "programId") UUID programId
    ){
        UUID userId = AuthContext.getUserId();

        if (!entryHeader.startsWith("Bearer ")) {
            throw new BookingException(BookingErrorCode.INVALID_ENTRY_TOKEN);
        }
        String entryToken = entryHeader.substring(7);

        // 입장 토큰 전용 시크릿으로 검증 후 반환
        BookingTokenClaims entryTokenClaims =  tokenProvider.validateEntryToken(entryToken);

        if(!entryTokenClaims.userId().equals(userId)){
            throw new BookingException(BookingErrorCode.INVALID_USER_ID);
        }

        if(!entryTokenClaims.programId().equals(programId)){
            throw new BookingException(BookingErrorCode.INVALID_PROGRAM_ID);
        }

        if (!entryTokenBlacklistService.tryBlacklist(entryToken, entryTokenClaims.expirationAt())) {
            throw new BookingException(BookingErrorCode.BLACKLISTED_ENTRY_TOKEN);
        }

        String sessionToken = tokenProvider.generateSessionToken(entryTokenClaims.userId(), entryTokenClaims.programId());

        return ApiResponse.success(CommonSuccessCode.OK, SessionResponse.of(sessionToken));
    }
}
