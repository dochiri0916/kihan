package com.example.kihan.application.auth.command;

public record LoginCommand(
        String email,
        String password
) {
}
