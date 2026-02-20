package com.dochiri.kihan.domain.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("RefreshToken 도메인 테스트")
class RefreshTokenTest {

    @Test
    @DisplayName("토큰 발급 시 필드가 올바르게 저장된다")
    void issue시_필드가_정상_설정된다() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 2, 21, 0, 0);

        RefreshToken refreshToken = RefreshToken.issue("token-1", 1L, expiresAt);

        assertEquals("token-1", refreshToken.getToken());
        assertEquals(1L, refreshToken.getUserId());
        assertEquals(expiresAt, refreshToken.getExpiresAt());
    }

    @Test
    @DisplayName("토큰 값이 null이면 발급에 실패한다")
    void issue시_token_null이면_예외() {
        assertThrows(
                NullPointerException.class,
                () -> RefreshToken.issue(null, 1L, LocalDateTime.of(2026, 2, 21, 0, 0))
        );
    }

    @Test
    @DisplayName("사용자 ID가 null이면 발급에 실패한다")
    void issue시_user_id_null이면_예외() {
        assertThrows(
                NullPointerException.class,
                () -> RefreshToken.issue("token", null, LocalDateTime.of(2026, 2, 21, 0, 0))
        );
    }

    @Test
    @DisplayName("만료 시각이 null이면 발급에 실패한다")
    void issue시_expires_at_null이면_예외() {
        assertThrows(
                NullPointerException.class,
                () -> RefreshToken.issue("token", 1L, null)
        );
    }

    @Test
    @DisplayName("현재 시각이 만료 시각 이후면 만료 예외가 발생한다")
    void verify_not_expired는_만료시_예외() {
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
    void verify_not_expired는_경계값_동일시간_통과() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );

        refreshToken.verifyNotExpired(LocalDateTime.of(2026, 2, 20, 10, 0));
    }

    @Test
    @DisplayName("토큰 소유자가 다르면 예외가 발생한다")
    void verify_ownership은_소유자_불일치시_예외() {
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
    void verify_ownership은_소유자_일치시_통과() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );

        refreshToken.verifyOwnership(1L);
    }

    @Test
    @DisplayName("토큰 회전 시 토큰 값과 만료 시각이 갱신된다")
    void rotate는_토큰과_만료시각을_갱신한다() {
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
    void rotate시_new_token_null이면_예외() {
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
    void rotate시_new_expires_at_null이면_예외() {
        RefreshToken refreshToken = RefreshToken.issue(
                "token",
                1L,
                LocalDateTime.of(2026, 2, 20, 10, 0)
        );

        assertThrows(NullPointerException.class, () -> refreshToken.rotate("token-2", null));
    }

}
