package com.dochiri.kihan.application.auth.command;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

public record IssueRefreshTokenCommand(
        String token,
        Long userId,
        LocalDateTime expiresAt
) {
    public IssueRefreshTokenCommand {
        requireNonNull(token);
        requireNonNull(userId);
        requireNonNull(expiresAt);
    }
}