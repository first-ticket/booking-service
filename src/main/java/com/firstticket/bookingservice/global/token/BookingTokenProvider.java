package com.firstticket.bookingservice.global.token;

import com.firstticket.bookingservice.booking.domain.exception.BookingErrorCode;
import com.firstticket.bookingservice.booking.domain.exception.BookingException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingTokenProvider {
    private final BookingTokenProperties bookingTokenProperties;

    private SecretKey getSigningKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getEntrySigningKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 예매 입장 토큰 검증 + claims 반환
    /* JWT의 페이로드에 다음과 같이 저장한다고 가정 -> Subject : 사용자 아이디, programId : 프로그램 아이디 // 추후 바뀔수도 있음
    * */
    public BookingTokenClaims validateEntryToken(String token){

        try{
            SecretKey signingKey = getEntrySigningKey(bookingTokenProperties.getEntrySecret());
            Claims claims =  Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return new BookingTokenClaims(UUID.fromString(claims.getSubject()), UUID.fromString(claims.get("programId", String.class)), claims.getExpiration());

        } catch (ExpiredJwtException e) {
            throw new BookingException(BookingErrorCode.EXPIRED_ENTRY_TOKEN); // 만료
        } catch (SignatureException e) {
            throw new BookingException(BookingErrorCode.TAMPERED_ENTRY_TOKEN); // 변조,  토큰 서명에 사용된 키와 검증에 사용된 키가 다를 때 발생
        } catch (MalformedJwtException | UnsupportedJwtException e) {
            throw new BookingException(BookingErrorCode.MALFORMED_ENTRY_TOKEN); // 형식 오류
        } catch (IllegalArgumentException e) {
            throw new BookingException(BookingErrorCode.EMPTY_ENTRY_TOKEN); // 없음
        }
    }
    // 세션 토큰 발급
    public String  generateSessionToken(UUID userId, UUID programId){
        SecretKey signingKey = getSigningKey(bookingTokenProperties.getSessionSecret());

        return Jwts.builder()
            .subject(userId.toString())
            .claim("programId",programId.toString())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + bookingTokenProperties.getSessionTokenValidity().toMillis()))
            .signWith(signingKey)
            .compact();

    }
    // 세션 토큰 검증 + claims 반환
    public BookingTokenClaims validateSessionToken(String token){

        try{
            SecretKey signingKey = getSigningKey(bookingTokenProperties.getSessionSecret());

            Claims claims =  Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return new BookingTokenClaims(UUID.fromString(claims.getSubject()), UUID.fromString(claims.get("programId", String.class)), claims.getExpiration());

        }catch (ExpiredJwtException e){
            throw new BookingException(BookingErrorCode.EXPIRED_SESSION_TOKEN);
        }catch (JwtException | IllegalArgumentException e) {
            // 그 외 모든 JWT 관련 에러 (변조, 형식 오류 등)
            throw new BookingException(BookingErrorCode.INVALID_SESSION_TOKEN);
        }

    }
}
