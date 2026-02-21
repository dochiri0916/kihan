package com.dochiri.kihan.application.auth.facade;

import com.dochiri.kihan.application.auth.dto.LoginResult;
import com.dochiri.kihan.domain.auth.RefreshToken;
import com.dochiri.kihan.domain.auth.RefreshTokenRepository;
import com.dochiri.kihan.domain.user.User;
import com.dochiri.kihan.domain.user.UserRepository;
import com.dochiri.kihan.infrastructure.security.jwt.JwtTokenGenerator;
import com.dochiri.kihan.infrastructure.security.jwt.JwtTokenResult;
import com.dochiri.kihan.infrastructure.security.jwt.RefreshTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReissueTokenFacade {

    private final RefreshTokenVerifier refreshTokenVerifier;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final Clock clock;

    @Transactional
    public LoginResult reissue(String refreshTokenValue) {
        Long userId = refreshTokenVerifier.verifyAndExtractUserId(refreshTokenValue);

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue);

        refreshToken.verifyNotExpired(LocalDateTime.now(clock));
        refreshToken.verifyOwnership(userId);

        User user = userRepository.findByIdAndDeletedAtIsNull(userId);

        JwtTokenResult tokenResult = jwtTokenGenerator.generateToken(user.getId(), user.getRole().name());

        refreshToken.rotate(tokenResult.refreshToken(), tokenResult.refreshTokenExpiresAt());
        refreshTokenRepository.save(refreshToken);

        return LoginResult.from(user, tokenResult.accessToken(), tokenResult.refreshToken());
    }

}
