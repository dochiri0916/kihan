package com.example.kihan.infrastructure.security.jwt;

public record JwtPrincipal(
        Long userId,
        String role
) {
}