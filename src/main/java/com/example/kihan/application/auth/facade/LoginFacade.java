package com.example.kihan.application.auth.facade;

import com.example.kihan.application.auth.command.IssueRefreshTokenCommand;
import com.example.kihan.application.auth.command.IssueRefreshTokenService;
import com.example.kihan.application.auth.command.LoginCommand;
import com.example.kihan.application.auth.command.UserAuthenticationService;
import com.example.kihan.application.auth.dto.LoginResult;
import com.example.kihan.domain.user.User;
import com.example.kihan.infrastructure.security.jwt.JwtTokenGenerator;
import com.example.kihan.infrastructure.security.jwt.JwtTokenResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoginFacade {

    private final UserAuthenticationService userAuthenticationService;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final IssueRefreshTokenService issueRefreshTokenService;

    @Transactional
    public LoginResult login(LoginCommand command) {
        User user = userAuthenticationService.execute(command);

        user.updateLastLoginAt();

        JwtTokenResult tokenResult = jwtTokenGenerator.generateToken(user.getId(), user.getRole().name());

        issueRefreshTokenService.execute(
                new IssueRefreshTokenCommand(
                        tokenResult.refreshToken(),
                        user.getId(),
                        tokenResult.refreshTokenExpiresAt()
                )
        );

        return LoginResult.from(user, tokenResult.accessToken(), tokenResult.refreshToken());
    }

}