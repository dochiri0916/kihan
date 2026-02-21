package com.dochiri.kihan.domain.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RefreshToken 도메인 테스트")
class RefreshTokenTest {

    @Test
    @DisplayName("토큰 발급 시 필드가 올바르게 저장된다")
    void shouldIssueRefreshTokenWithValidFields() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 2, 21, 0, 0);

        RefreshToken refreshToken = RefreshToken.issue("token-1", 1L, expiresAt);

        assertEquals("token-1", refreshToken.getToken());
        assertEquals(1L, refreshToken.getUserId());
        assertEquals(expiresAt, refreshToken.getExpiresAt());
    }

    @Test
    @DisplayName("토큰 값이 null이면 발급에 실패한다")
    void shouldThrowWhenIssuingWithNullToken() {
        assertThrows(
                NullPointerException.class,
                () -> RefreshToken.issue(null, 1L, LocalDateTime.of(2026, 2, 21, 0, 0))
        );
    }

    @Test
    @DisplayName("사용자 ID가 null이면 발급에 실패한다")
    void shouldThrowWhenIssuingWithNullUserId() {
        assertThrows(
                NullPointerException.class,
                () -> RefreshToken.issue("token", null, LocalDateTime.of(2026, 2, 21, 0, 0))
        );
    }

    @Test
    @DisplayName("만료 시각이 null이면 발급에 실패한다")
    void shouldThrowWhenIssuingWithNullExpiresAt() {
        assertThrows(
                NullPointerException.class,
                () -> RefreshToken.issue("token", 1L, null)
        );
    }

    @Test
    @DisplayName("현재 시각이 만료 시각 이후면 만료 예외가 발생한다")
    void shouldThrowWhenTokenIsExpired() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );

        assertThrows(
                ExpiredRefreshTokenException.class,
                () -> refreshToken.verifyNotExpired(LocalDateTime.of(2026, 2, 20, 10, 1))
        );
    }

    @Test
    @DisplayName("현재 시각이 만료 시각과 같으면 유효하다")
    void shouldPassWhenNowEqualsExpiresAt() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );

        refreshToken.verifyNotExpired(LocalDateTime.of(2026, 2, 20, 10, 0));
    }

    @Test
    @DisplayName("현재 시각이 만료 시각 이전이면 유효하다")
    void shouldPassWhenNowIsBeforeExpiresAt() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );

        refreshToken.verifyNotExpired(LocalDateTime.of(2026, 2, 20, 9, 59));
    }

    @Test
    @DisplayName("현재 시각이 null이면 만료 검증에서 예외가 발생한다")
    void shouldThrowWhenVerifyingExpiryWithNullNow() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );

        assertThrows(NullPointerException.class, () -> refreshToken.verifyNotExpired(null));
    }

    @Test
    @DisplayName("토큰 소유자가 다르면 예외가 발생한다")
    void shouldThrowWhenOwnershipDoesNotMatch() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshToken.verifyOwnership(2L)
        );
    }

    @Test
    @DisplayName("토큰 소유자가 같으면 검증을 통과한다")
    void shouldPassWhenOwnershipMatches() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );

        refreshToken.verifyOwnership(1L);
    }

    @Test
    @DisplayName("토큰 소유자 검증에서 사용자 ID가 null이면 소유자 불일치 예외가 발생한다")
    void shouldThrowWhenVerifyingOwnershipWithNullUserId() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshToken.verifyOwnership(null)
        );
    }

    @Test
    @DisplayName("토큰 회전 시 토큰 값과 만료 시각이 갱신된다")
    void shouldRotateTokenAndExpiresAt() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );
        LocalDateTime newExpiresAt = LocalDateTime.of(2026, 2, 21, 10, 0);

        refreshToken.rotate("token-2", newExpiresAt);

        assertEquals("token-2", refreshToken.getToken());
        assertEquals(newExpiresAt, refreshToken.getExpiresAt());
    }

    @Test
    @DisplayName("회전 시 새 토큰이 null이면 예외가 발생한다")
    void shouldThrowWhenRotatingWithNullToken() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );

        assertThrows(
                NullPointerException.class,
                () -> refreshToken.rotate(null, LocalDateTime.of(2026, 2, 21, 10, 0))
        );
    }

    @Test
    @DisplayName("회전 시 새 만료 시각이 null이면 예외가 발생한다")
    void shouldThrowWhenRotatingWithNullExpiresAt() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );

        assertThrows(NullPointerException.class, () -> refreshToken.rotate("token-2", null));
    }

    @Test
    @DisplayName("InvalidRefreshTokenException.invalidTokenType 메시지를 검증한다")
    void shouldContainMessageForInvalidTokenType() {
        InvalidRefreshTokenException exception = InvalidRefreshTokenException.invalidTokenType();

        assertTrue(exception.getMessage().contains("리프레시 토큰"));
    }

    @Test
    @DisplayName("RefreshTokenNotFoundException 메시지를 검증한다")
    void shouldContainMessageForRefreshTokenNotFound() {
        RefreshTokenNotFoundException exception = new RefreshTokenNotFoundException();

        assertTrue(exception.getMessage().contains("만료된 토큰"));
    }

    @Test
    @DisplayName("ExpiredRefreshTokenException 메시지를 검증한다")
    void shouldContainMessageForExpiredRefreshToken() {
        ExpiredRefreshTokenException exception = new ExpiredRefreshTokenException();

        assertTrue(exception.getMessage().contains("만료된 리프레시 토큰"));
    }

}
