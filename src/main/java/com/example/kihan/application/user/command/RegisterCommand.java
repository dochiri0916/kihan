package com.example.kihan.application.user.command;

public record RegisterCommand(
        String email,
        String password,
        String name
) {
}
