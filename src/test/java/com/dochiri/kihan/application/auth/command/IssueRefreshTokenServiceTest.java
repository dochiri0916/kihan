package com.dochiri.kihan.application.auth.command;

import com.dochiri.kihan.domain.auth.RefreshToken;
import com.dochiri.kihan.domain.auth.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("IssueRefreshTokenService 테스트")
class IssueRefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private IssueRefreshTokenService issueRefreshTokenService;

    @Test
    @DisplayName("기존 토큰이 없으면 새 토큰을 발급 후 저장한다")
    void shouldIssueAndSaveWhenNoExistingRefreshToken() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 2, 25, 10, 0);
        IssueRefreshTokenCommand command = new IssueRefreshTokenCommand("new-token", 1L, expiresAt);
        when(refreshTokenRepository.findByUserId(1L)).thenReturn(Optional.empty());

        issueRefreshTokenService.execute(command);

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("기존 토큰이 있으면 회전 후 저장한다")
    void shouldRotateAndSaveWhenExistingRefreshTokenExists() {
        RefreshToken existing = RefreshToken.issue(
                "old-token",
                1L,
                LocalDateTime.of(2026, 2, 22, 10, 0)
        );
        LocalDateTime newExpiresAt = LocalDateTime.of(2026, 2, 25, 10, 0);
        IssueRefreshTokenCommand command = new IssueRefreshTokenCommand("new-token", 1L, newExpiresAt);
        when(refreshTokenRepository.findByUserId(1L)).thenReturn(Optional.of(existing));

        issueRefreshTokenService.execute(command);

        assertEquals("new-token", existing.getToken());
        assertEquals(newExpiresAt, existing.getExpiresAt());
        verify(refreshTokenRepository).save(existing);
    }
}
