package com.example.kihan.infrastructure.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtTokenGenerator {

    private final JwtProvider jwtProvider;

    public JwtTokenResult generateToken(final Long userId, final String role) {
        String accessToken = jwtProvider.generateAccessToken(userId, role);
        String refreshToken = jwtProvider.generateRefreshToken(userId, role);

        LocalDateTime refreshExpiresAt = jwtProvider.refreshTokenExpiresAt();

        return new JwtTokenResult(accessToken, refreshToken, refreshExpiresAt);
    }

    public String generateAccessToken(final Long userId, final String role) {
        return jwtProvider.generateAccessToken(userId, role);
    }

}