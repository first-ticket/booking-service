package com.firstticket.bookingservice.global.resolver;

import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import com.firstticket.bookingservice.global.annotation.BookingToken;
import com.firstticket.bookingservice.global.token.BookingTokenClaims;
import com.firstticket.bookingservice.global.token.BookingTokenProvider;
import java.util.UUID;

import com.firstticket.common.web.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/*
* 세션 토큰 검증만 담당 : 토큰에 담긴 사용자 vs 실제 사용자 비교
* */
@Component
@RequiredArgsConstructor
public class BookingTokenResolver implements HandlerMethodArgumentResolver {

    private final BookingTokenProvider tokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(BookingToken.class);
    }

    @Override
    public BookingTokenClaims resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        // 세션 토큰 검증
        String sessionHeader = webRequest.getHeader("Booking-Session-Token");

        if (sessionHeader == null || !sessionHeader.startsWith("Bearer ")) {
            throw new BookingException(BookingErrorCode.EMPTY_SESSION_TOKEN);
        }

        String token = sessionHeader.substring(7).trim();
        if(token.isEmpty()){
            throw new BookingException(BookingErrorCode.EMPTY_SESSION_TOKEN);
        }

        // 세션 토큰 전용 시크릿으로 검증 후 반환
        BookingTokenClaims sessionTokenClaims = tokenProvider.validateSessionToken(token);

        UUID userId = AuthContext.getUserId();

        if (!sessionTokenClaims.userId().equals(userId)) {
            throw new BookingException(BookingErrorCode.INVALID_USER_ID);
        }

        return sessionTokenClaims;
    }
}
