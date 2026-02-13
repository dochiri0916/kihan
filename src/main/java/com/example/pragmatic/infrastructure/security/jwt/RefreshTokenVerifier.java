package com.example.pragmatic.infrastructure.security.jwt;

import com.example.pragmatic.domain.auth.InvalidRefreshTokenException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenVerifier {

    private final JwtProvider jwtProvider;

    public Long verifyAndExtractUserId(String refreshToken) {
        Claims claims = jwtProvider.parseAndValidate(refreshToken);

        if (!jwtProvider.isRefreshToken(claims)) {
            throw InvalidRefreshTokenException.invalidTokenType();
        }

        return jwtProvider.extractUserId(claims);
    }

}