package com.dochiri.kihan.application.auth.command;

import com.dochiri.kihan.domain.auth.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RevokeTokenService 테스트")
class RevokeTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RevokeTokenService revokeTokenService;

    @Test
    @DisplayName("만료 시각 이전 토큰 삭제 개수를 반환한다")
    void shouldReturnDeletedCountWhenRevokingExpiredTokens() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 25, 0, 0);
        when(refreshTokenRepository.deleteByExpiresAtBefore(now)).thenReturn(3L);

        long deleted = revokeTokenService.execute(new RevokeTokenCommand(now));

        assertEquals(3L, deleted);
        verify(refreshTokenRepository).deleteByExpiresAtBefore(now);
    }

    @Test
    @DisplayName("토큰 문자열로 삭제 개수를 반환한다")
    void shouldReturnDeletedCountWhenRevokingByToken() {
        when(refreshTokenRepository.deleteByToken("token-x")).thenReturn(1L);

        long deleted = revokeTokenService.revokeByToken("token-x");

        assertEquals(1L, deleted);
        verify(refreshTokenRepository).deleteByToken("token-x");
    }
}
