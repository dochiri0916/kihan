package com.dochiri.kihan.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("User 도메인 테스트")
class UserTest {

    @Test
    @DisplayName("회원가입 시 기본 역할은 USER다")
    void register시_기본_역할은_user다() {
        User user = User.register("test@example.com", "password", "홍길동");

        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals("홍길동", user.getName());
        assertEquals(UserRole.USER, user.getRole());
    }

    @Test
    @DisplayName("회원가입 시 email이 null이면 예외가 발생한다")
    void register시_email_null이면_예외() {
        assertThrows(NullPointerException.class, () -> User.register(null, "password", "홍길동"));
    }

    @Test
    @DisplayName("회원가입 시 password가 null이면 예외가 발생한다")
    void register시_password_null이면_예외() {
        assertThrows(NullPointerException.class, () -> User.register("test@example.com", null, "홍길동"));
    }

    @Test
    @DisplayName("회원가입 시 name이 null이면 예외가 발생한다")
    void register시_name_null이면_예외() {
        assertThrows(NullPointerException.class, () -> User.register("test@example.com", "password", null));
    }

    @Test
    @DisplayName("마지막 로그인 시각을 업데이트할 수 있다")
    void update_last_login_at은_시각을_설정한다() {
        User user = User.register("test@example.com", "password", "홍길동");
        LocalDateTime now = LocalDateTime.of(2026, 2, 20, 22, 0);

        user.updateLastLoginAt(now);

        assertEquals(now, user.getLastLoginAt());
    }

    @Test
    @DisplayName("마지막 로그인 시각이 null이면 예외가 발생한다")
    void update_last_login_at에서_null이면_예외() {
        User user = User.register("test@example.com", "password", "홍길동");

        assertThrows(NullPointerException.class, () -> user.updateLastLoginAt(null));
    }

    @Test
    @DisplayName("UserAccessDeniedException 팩토리는 사용자 ID를 메시지에 포함한다")
    void user_access_denied_exception_메시지_검증() {
        UserAccessDeniedException exception = UserAccessDeniedException.forUser(99L);

        assertTrue(exception.getMessage().contains("99"));
    }

}
