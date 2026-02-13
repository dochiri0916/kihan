package com.example.pragmatic.infrastructure.security.jwt;

public record JwtPrincipal(
        Long userId,
        String role
) {
}