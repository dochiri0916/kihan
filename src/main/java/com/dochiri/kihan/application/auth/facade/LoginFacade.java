package com.dochiri.kihan.application.auth.facade;

import com.dochiri.kihan.application.auth.command.IssueRefreshTokenCommand;
import com.dochiri.kihan.application.auth.command.IssueRefreshTokenService;
import com.dochiri.kihan.application.auth.command.LoginCommand;
import com.dochiri.kihan.application.auth.command.UserAuthenticationService;
import com.dochiri.kihan.application.auth.dto.LoginResult;
import com.dochiri.kihan.domain.user.User;
import com.dochiri.kihan.infrastructure.security.jwt.JwtTokenGenerator;
import com.dochiri.kihan.infrastructure.security.jwt.JwtTokenResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LoginFacade {

    private final UserAuthenticationService userAuthenticationService;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final IssueRefreshTokenService issueRefreshTokenService;
    private final Clock clock;

    @Transactional
    public LoginResult login(LoginCommand command) {
        User user = userAuthenticationService.execute(command);

        user.updateLastLoginAt(LocalDateTime.now(clock));

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
