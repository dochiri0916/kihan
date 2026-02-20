package com.example.kihan.application.auth.command;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

public record RevokeTokenCommand(
        LocalDateTime now
) {
    public RevokeTokenCommand {
        requireNonNull(now);
    }
}
