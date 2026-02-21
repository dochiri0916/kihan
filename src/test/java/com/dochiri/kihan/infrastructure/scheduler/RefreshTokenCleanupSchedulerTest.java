package com.dochiri.kihan.infrastructure.scheduler;

import com.dochiri.kihan.application.auth.command.RevokeTokenCommand;
import com.dochiri.kihan.application.auth.command.RevokeTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenCleanupScheduler 테스트")
class RefreshTokenCleanupSchedulerTest {

    @Mock
    private RevokeTokenService revokeTokenService;

    @InjectMocks
    private RefreshTokenCleanupScheduler refreshTokenCleanupScheduler;

    @Test
    @DisplayName("스케줄 실행 시 현재 시각 기준으로 만료 토큰 정리를 호출한다")
    void shouldInvokeRevokeTokenServiceWithCurrentTime() {
        when(revokeTokenService.execute(org.mockito.ArgumentMatchers.any(RevokeTokenCommand.class))).thenReturn(5L);

        refreshTokenCleanupScheduler.cleanupExpiredRefreshTokens();

        ArgumentCaptor<RevokeTokenCommand> captor = ArgumentCaptor.forClass(RevokeTokenCommand.class);
        verify(revokeTokenService).execute(captor.capture());
        assertTrue(captor.getValue().now() != null);
    }
}
