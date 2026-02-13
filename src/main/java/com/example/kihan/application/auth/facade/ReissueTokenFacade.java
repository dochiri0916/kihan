package com.example.kihan.application.auth.facade;

import com.example.kihan.application.auth.query.RefreshTokenLoader;
import com.example.kihan.application.user.query.UserLoader;
import com.example.kihan.domain.auth.InvalidRefreshTokenException;
import com.example.kihan.domain.auth.RefreshToken;
import com.example.kihan.domain.user.User;
import com.example.kihan.infrastructure.security.jwt.JwtTokenGenerator;
import com.example.kihan.infrastructure.security.jwt.RefreshTokenVerifier;
import com.example.kihan.presentation.auth.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReissueTokenFacade {

    private final RefreshTokenVerifier refreshTokenVerifier;
    private final RefreshTokenLoader refreshTokenLoader;
    private final UserLoader userLoader;
    private final JwtTokenGenerator jwtTokenGenerator;

    public AuthResponse reissue(final String refreshTokenValue) {
        Long userId = refreshTokenVerifier.verifyAndExtractUserId(refreshTokenValue);

        RefreshToken refreshToken = refreshTokenLoader.loadValidToken(
                refreshTokenValue,
                LocalDateTime.now()
        );

        if (!refreshToken.isOwnedBy(userId)) {
            throw InvalidRefreshTokenException.ownerMismatch();
        }

        User user = userLoader.loadActiveUserById(userId);

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