package org.example.badhabitzero.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.badhabitzero.global.error.BusinessException;
import org.example.badhabitzero.global.error.ErrorCode;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    // ========== 토큰 생성 ==========
    public String createToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))           // 토큰 주체 (사용자 ID)
                .claim("email", email)                     // 추가 정보
                .issuedAt(now)                             // 발행 시간
                .expiration(expiry)                        // 만료 시간
                .signWith(getSigningKey())                 // 서명
                .compact();
    }

    // ========== 토큰에서 사용자 ID 추출 ==========
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    // ========== 토큰에서 이메일 추출 ==========
    public String getEmail(String token) {
        Claims claims = parseClaims(token);
        return claims.get("email", String.class);
    }

    // ========== 토큰 유효성 검증 ==========
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    // ========== 토큰 파싱 ==========
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ========== 서명 키 생성 ==========
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}