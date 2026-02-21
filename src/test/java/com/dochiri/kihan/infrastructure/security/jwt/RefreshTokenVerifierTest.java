package com.dochiri.kihan.infrastructure.security.jwt;

import com.dochiri.kihan.domain.auth.InvalidRefreshTokenException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenVerifier 테스트")
class RefreshTokenVerifierTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private Claims claims;

    @InjectMocks
    private RefreshTokenVerifier refreshTokenVerifier;

    @Test
    @DisplayName("리프레시 토큰이면 사용자 ID를 추출해 반환한다")
    void shouldExtractUserIdWhenTokenIsRefreshToken() {
        when(jwtProvider.parseAndValidate("refresh")).thenReturn(claims);
        when(jwtProvider.isRefreshToken(claims)).thenReturn(true);
        when(jwtProvider.extractUserId(claims)).thenReturn(7L);

        Long userId = refreshTokenVerifier.verifyAndExtractUserId("refresh");

        assertEquals(7L, userId);
    }

    @Test
    @DisplayName("리프레시 토큰이 아니면 예외를 던진다")
    void shouldThrowWhenTokenIsNotRefreshToken() {
        when(jwtProvider.parseAndValidate("access")).thenReturn(claims);
        when(jwtProvider.isRefreshToken(claims)).thenReturn(false);

        assertThrows(InvalidRefreshTokenException.class,
                () -> refreshTokenVerifier.verifyAndExtractUserId("access"));
    }
}
