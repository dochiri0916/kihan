package com.dochiri.kihan.infrastructure.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenGenerator 테스트")
class JwtTokenGeneratorTest {

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private JwtTokenGenerator jwtTokenGenerator;

    @Test
    @DisplayName("토큰 생성 시 access, refresh, 만료 시각을 묶어 반환한다")
    void shouldGenerateAccessRefreshAndExpiryTogether() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 2, 28, 0, 0);
        when(jwtProvider.generateAccessToken(1L, "USER")).thenReturn("access");
        when(jwtProvider.generateRefreshToken(1L, "USER")).thenReturn("refresh");
        when(jwtProvider.refreshTokenExpiresAt()).thenReturn(expiresAt);

        JwtTokenResult result = jwtTokenGenerator.generateToken(1L, "USER");

        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());
        assertEquals(expiresAt, result.refreshTokenExpiresAt());
    }

    @Test
    @DisplayName("access 토큰 단독 생성 요청을 위임한다")
    void shouldDelegateAccessTokenGeneration() {
        when(jwtProvider.generateAccessToken(2L, "ADMIN")).thenReturn("access-admin");

        String token = jwtTokenGenerator.generateAccessToken(2L, "ADMIN");

        assertEquals("access-admin", token);
    }
}
