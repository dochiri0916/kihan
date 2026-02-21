package com.dochiri.kihan.infrastructure.security.jwt;

import com.dochiri.kihan.infrastructure.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JwtProvider 테스트")
class JwtProviderTest {

    private final JwtProvider jwtProvider = new JwtProvider(
            new JwtProperties(
                    "0123456789012345678901234567890123456789012345678901234567890123",
                    60_000,
                    120_000
            )
    );

    @Test
    @DisplayName("access 토큰 생성 후 파싱하면 사용자 ID와 role을 추출할 수 있다")
    void shouldExtractUserIdAndRoleFromAccessToken() {
        String token = jwtProvider.generateAccessToken(11L, "USER");

        Claims claims = jwtProvider.parseAndValidate(token);

        assertEquals(11L, jwtProvider.extractUserId(claims));
        assertEquals("USER", jwtProvider.extractRole(claims));
        assertTrue(jwtProvider.isAccessToken(claims));
        assertTrue(!jwtProvider.isRefreshToken(claims));
    }

    @Test
    @DisplayName("refresh 토큰 생성 후 파싱하면 refresh 카테고리로 인식한다")
    void shouldRecognizeRefreshTokenCategory() {
        String token = jwtProvider.generateRefreshToken(22L, "ADMIN");

        Claims claims = jwtProvider.parseAndValidate(token);

        assertEquals(22L, jwtProvider.extractUserId(claims));
        assertEquals("ADMIN", jwtProvider.extractRole(claims));
        assertTrue(jwtProvider.isRefreshToken(claims));
        assertTrue(!jwtProvider.isAccessToken(claims));
    }

    @Test
    @DisplayName("refreshTokenExpiresAt은 현재 시각 이후를 반환한다")
    void shouldReturnFutureTimeForRefreshTokenExpiresAt() {
        LocalDateTime before = LocalDateTime.now();

        LocalDateTime expiresAt = jwtProvider.refreshTokenExpiresAt();

        assertTrue(expiresAt.isAfter(before));
    }
}
