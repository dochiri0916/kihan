package com.dochiri.kihan.infrastructure.scheduler;

import com.dochiri.kihan.application.auth.command.RevokeTokenCommand;
import com.dochiri.kihan.application.auth.command.RevokeTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RevokeTokenService revokeTokenService;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredRefreshTokens() {
        long deletedCount = revokeTokenService.execute(new RevokeTokenCommand(LocalDateTime.now()));
        log.info("Expired refresh tokens revoked. count={}", deletedCount);
    }

}
