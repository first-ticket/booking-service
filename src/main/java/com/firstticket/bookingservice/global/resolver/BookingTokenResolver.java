package com.firstticket.bookingservice.global.resolver;

import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import com.firstticket.bookingservice.global.annotation.BookingToken;
import com.firstticket.bookingservice.global.token.BookingTokenClaims;
import com.firstticket.bookingservice.global.token.BookingTokenProvider;
import com.firstticket.bookingservice.global.token.EntryTokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

/*
* 현재 사용자가 어떤 토큰을 가지고 있는지 확인하고 BookingTokenProvider을 호출하는 역할만 담당
    * 1. 예매 세션 토큰을 가지고 있으면 그대로 진행
    * 2. 예매 세션 토큰이 없이 예매 입장 토큰이 있으면 -> 예매 세션 토큰 발급
    * 3. 둘 다 없으면 실패
* */
@Component
@RequiredArgsConstructor
public class BookingTokenResolver implements HandlerMethodArgumentResolver {

    private final BookingTokenProvider tokenProvider;
    private final EntryTokenBlacklistService entryTokenBlacklistService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(BookingToken.class);
    }

    @Override
    public BookingTokenClaims resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        HttpServletResponse response = (HttpServletResponse) webRequest.getNativeResponse();

        String xUserId = webRequest.getHeader("X-User-Id");
        if(xUserId == null){
            throw new BookingException(BookingErrorCode.EMPTY_X_USER_ID);
        }

        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if(pathVariables == null || pathVariables.get("programId") == null) {
            throw new BookingException(BookingErrorCode.EMPTY_PATHVARIABLE);
        }
        UUID programId = UUID.fromString(pathVariables.get("programId"));


        // 1. 세션 토큰 확인 (우선순위 높음)
        String sessionHeader = webRequest.getHeader("Booking-Session-Token");
        if (sessionHeader != null && sessionHeader.startsWith("Bearer ")) {
            String token = sessionHeader.substring(7);
            // 세션 토큰 전용 시크릿으로 검증 후 반환
            BookingTokenClaims sessionTokenClaims = tokenProvider.validateSessionToken(token);



            if(!sessionTokenClaims.userId().equals(UUID.fromString(xUserId))){
                throw new BookingException(BookingErrorCode.INVALID_USER_ID);
            }

            if(!sessionTokenClaims.programId().equals(programId)){
                throw new BookingException(BookingErrorCode.INVALID_PROGRAM_ID);
            }

            return sessionTokenClaims;
        }

        // 2. 입장 토큰 확인 (세션 토큰이 없을 때 실행)
        String entryHeader = webRequest.getHeader("Booking-Entry-Token");
        if (entryHeader != null && entryHeader.startsWith("Bearer ")) {
            String entryToken = entryHeader.substring(7);
            if(entryTokenBlacklistService.isBlacklisted(entryToken)){
                throw new BookingException(BookingErrorCode.BLACKLISTED_ENTRY_TOKEN);
            }
            // 입장 토큰 전용 시크릿으로 검증 후 반환
            BookingTokenClaims entryTokenClaims =  tokenProvider.validateEntryToken(entryToken);

            if(!entryTokenClaims.userId().equals(UUID.fromString(xUserId))){
                throw new BookingException(BookingErrorCode.INVALID_USER_ID);
            }

            if(!entryTokenClaims.programId().equals(programId)){
                throw new BookingException(BookingErrorCode.INVALID_PROGRAM_ID);
            }

            //TODO 해당 프로그램ID가 유효한 프로그램ID인지 프로그램서버 조회 (추후 도입)

            String newToken = tokenProvider.generateSessionToken(entryTokenClaims.userId(), entryTokenClaims.programId());

            response.setHeader("Booking-Session-Token", "Bearer " + newToken);
            response.setHeader("Booking-Entry-Token","");
            entryTokenBlacklistService.blacklist(entryToken, entryTokenClaims.expirationAt());

            return entryTokenClaims;
        }

        // 3. 둘 다 없으면 예외 발생
        throw new BookingException(BookingErrorCode.EMPTY_TOKEN);
    }
}
