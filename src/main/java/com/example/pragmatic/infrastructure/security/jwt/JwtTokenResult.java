package com.example.pragmatic.infrastructure.security.jwt;

import java.time.LocalDateTime;

public record JwtTokenResult(
        String accessToken,
        String refreshToken,
        LocalDateTime refreshTokenExpiresAt
) {
}