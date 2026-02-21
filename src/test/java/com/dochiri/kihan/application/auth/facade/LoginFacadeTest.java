package com.dochiri.kihan.application.auth.facade;

import com.dochiri.kihan.application.auth.command.IssueRefreshTokenCommand;
import com.dochiri.kihan.application.auth.command.IssueRefreshTokenService;
import com.dochiri.kihan.application.auth.command.LoginCommand;
import com.dochiri.kihan.application.auth.command.UserAuthenticationService;
import com.dochiri.kihan.application.auth.dto.LoginResult;
import com.dochiri.kihan.domain.user.User;
import com.dochiri.kihan.infrastructure.security.jwt.JwtTokenGenerator;
import com.dochiri.kihan.infrastructure.security.jwt.JwtTokenResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginFacade 테스트")
class LoginFacadeTest {

    @Mock
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private JwtTokenGenerator jwtTokenGenerator;

    @Mock
    private IssueRefreshTokenService issueRefreshTokenService;

    @Mock
    private Clock clock;

    @InjectMocks
    private LoginFacade loginFacade;

    @Test
    @DisplayName("로그인 성공 시 마지막 로그인 시각을 갱신하고 토큰을 발급한다")
    void shouldUpdateLastLoginAndIssueTokensWhenLoginSucceeds() {
        LoginCommand command = new LoginCommand("a@a.com", "pw");
        User user = User.register("a@a.com", "encoded", "alice");
        setId(user, 1L);

        Instant fixedInstant = Instant.parse("2026-02-21T00:00:00Z");
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        LocalDateTime refreshExpiresAt = LocalDateTime.of(2026, 2, 28, 0, 0);
        JwtTokenResult tokenResult = new JwtTokenResult("access-x", "refresh-x", refreshExpiresAt);

        when(userAuthenticationService.execute(command)).thenReturn(user);
        when(jwtTokenGenerator.generateToken(1L, "USER")).thenReturn(tokenResult);

        LoginResult result = loginFacade.login(command);

        assertEquals("access-x", result.accessToken());
        assertEquals("refresh-x", result.refreshToken());
        assertEquals(LocalDateTime.of(2026, 2, 21, 0, 0), user.getLastLoginAt());

        ArgumentCaptor<IssueRefreshTokenCommand> captor = ArgumentCaptor.forClass(IssueRefreshTokenCommand.class);
        verify(issueRefreshTokenService).execute(captor.capture());

        IssueRefreshTokenCommand issued = captor.getValue();
        assertEquals("refresh-x", issued.token());
        assertEquals(1L, issued.userId());
        assertEquals(refreshExpiresAt, issued.expiresAt());
    }

    private void setId(User user, Long id) {
        try {
            Field idField = user.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }
}
