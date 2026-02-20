package com.dochiri.kihan.infrastructure.security.jwt;

public record JwtPrincipal(
        Long userId,
        String role
) {
}