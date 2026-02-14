package com.example.kihan.application.auth.dto;

public record AuthResult(
        Long userId,
        String role,
        String accessToken
) {
}
