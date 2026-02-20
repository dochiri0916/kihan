package com.example.kihan.application.user.command;

import static java.util.Objects.requireNonNull;

public record RegisterUserCommand(
        String email,
        String password,
        String name
) {
    public RegisterUserCommand {
        requireNonNull(email);
        requireNonNull(password);
        requireNonNull(name);
    }
}