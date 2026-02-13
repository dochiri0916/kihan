package com.example.pragmatic.application.auth.facade;

import com.example.pragmatic.application.auth.query.RefreshTokenQueryService;
import com.example.pragmatic.application.user.query.UserFinder;
import com.example.pragmatic.domain.auth.InvalidRefreshTokenException;
import com.example.pragmatic.domain.auth.RefreshToken;
import com.example.pragmatic.domain.user.User;
import com.example.pragmatic.infrastructure.security.jwt.JwtProvider;
import com.example.pragmatic.infrastructure.security.jwt.JwtTokenGenerator;
import com.example.pragmatic.infrastructure.security.jwt.RefreshTokenVerifier;
import com.example.pragmatic.presentation.auth.response.AuthResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReissueTokenFacade {

    private final RefreshTokenVerifier refreshTokenVerifier;
    private final RefreshTokenQueryService refreshTokenQueryService;
    private final UserFinder userFinder;
    private final JwtTokenGenerator jwtTokenGenerator;

    public AuthResponse reissue(final String refreshTokenValue) {
        Long userId = refreshTokenVerifier.verifyAndExtractUserId(refreshTokenValue);

        RefreshToken refreshToken = refreshTokenQueryService.getValidToken(
                refreshTokenValue,
                LocalDateTime.now()
        );

        if (!refreshToken.isOwnedBy(userId)) {
            throw InvalidRefreshTokenException.ownerMismatch();
        }

        User user = userFinder.findActiveUserById(userId);

        String newAccessToken = jwtTokenGenerator.generateAccessToken(
                user.getId(),
                user.getRole().name()
        );

        return AuthResponse.from(
                user,
                newAccessToken
        );
    }

}