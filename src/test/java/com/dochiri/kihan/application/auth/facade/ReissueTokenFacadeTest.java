package com.dochiri.kihan.application.auth.facade;

import com.dochiri.kihan.application.auth.dto.LoginResult;
import com.dochiri.kihan.domain.auth.exception.InvalidRefreshTokenException;
import com.dochiri.kihan.domain.auth.RefreshToken;
import com.dochiri.kihan.domain.auth.RefreshTokenRepository;
import com.dochiri.kihan.domain.user.User;
import com.dochiri.kihan.domain.user.UserRepository;
import com.dochiri.kihan.infrastructure.security.jwt.JwtTokenGenerator;
import com.dochiri.kihan.infrastructure.security.jwt.JwtTokenResult;
import com.dochiri.kihan.infrastructure.security.jwt.RefreshTokenVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReissueTokenFacade 테스트")
class ReissueTokenFacadeTest {

    @Mock
    private RefreshTokenVerifier refreshTokenVerifier;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenGenerator jwtTokenGenerator;

    @Mock
    private Clock clock;

    @InjectMocks
    private ReissueTokenFacade reissueTokenFacade;

    @Test
    @DisplayName("리프레시 토큰 재발급 성공 시 토큰을 회전하고 저장한다")
    void shouldRotateAndSaveRefreshTokenWhenReissueSucceeds() {
        String refreshTokenValue = "refresh-old";
        when(refreshTokenVerifier.verifyAndExtractUserId(refreshTokenValue)).thenReturn(1L);
        when(clock.instant()).thenReturn(Instant.parse("2026-02-21T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        RefreshToken stored = RefreshToken.issue(
                refreshTokenValue,
                1L,
                LocalDateTime.of(2099, 1, 1, 0, 0)
        );
        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(stored);

        User user = User.register("a@a.com", "pw", "alice");
        setId(user, 1L);
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(user);

        JwtTokenResult tokenResult = new JwtTokenResult(
                "access-new",
                "refresh-new",
                LocalDateTime.of(2099, 1, 2, 0, 0)
        );
        when(jwtTokenGenerator.generateToken(1L, "USER")).thenReturn(tokenResult);

        LoginResult result = reissueTokenFacade.reissue(refreshTokenValue);

        assertEquals("access-new", result.accessToken());
        assertEquals("refresh-new", result.refreshToken());
        assertEquals("refresh-new", stored.getToken());
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    @DisplayName("토큰 소유자가 불일치하면 예외를 던지고 저장하지 않는다")
    void shouldThrowAndNotSaveWhenRefreshTokenOwnerMismatches() {
        String refreshTokenValue = "refresh-old";
        when(refreshTokenVerifier.verifyAndExtractUserId(refreshTokenValue)).thenReturn(2L);
        when(clock.instant()).thenReturn(Instant.parse("2026-02-21T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        RefreshToken stored = RefreshToken.issue(
                refreshTokenValue,
                1L,
                LocalDateTime.of(2099, 1, 1, 0, 0)
        );
        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(stored);

        assertThrows(InvalidRefreshTokenException.class, () -> reissueTokenFacade.reissue(refreshTokenValue));

        verify(refreshTokenRepository, never()).save(stored);
        verify(userRepository, never()).findByIdAndDeletedAtIsNull(2L);
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
