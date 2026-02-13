package com.example.kihan.application.auth.facade;

import com.example.kihan.application.auth.command.RefreshTokenIssueService;
import com.example.kihan.application.auth.command.UserAuthenticationService;
import com.example.kihan.application.auth.dto.LoginResult;
import com.example.kihan.domain.user.User;
import com.example.kihan.infrastructure.security.jwt.JwtTokenGenerator;
import com.example.kihan.infrastructure.security.jwt.JwtTokenResult;
import com.example.kihan.presentation.auth.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoginFacade {

    private final UserAuthenticationService userAuthenticationService;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final RefreshTokenIssueService refreshTokenIssueService;

    @Transactional
    public LoginResult login(final LoginRequest request) {
        User user = userAuthenticationService.authenticate(request);

        user.updateLastLoginAt();

        JwtTokenResult tokenResult = jwtTokenGenerator.generateToken(user.getId(), user.getRole().name());

        refreshTokenIssueService.issue(
                tokenResult.refreshToken(),
                user.getId(),
                tokenResult.refreshTokenExpiresAt()
        );

        return new LoginResult(user, tokenResult.accessToken(), tokenResult.refreshToken());
    }

}