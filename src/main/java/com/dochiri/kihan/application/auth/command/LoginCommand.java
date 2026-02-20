package com.dochiri.kihan.application.auth.command;

import static java.util.Objects.requireNonNull;

public record LoginCommand(
        String email,
        String password
) {
    public LoginCommand {
        requireNonNull(email);
        requireNonNull(password);
    }
}