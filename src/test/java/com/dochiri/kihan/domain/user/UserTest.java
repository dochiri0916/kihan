package com.dochiri.kihan.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("User 도메인 테스트")
class UserTest {

    @Test
    @DisplayName("회원가입 시 기본 역할은 USER다")
    void shouldSetDefaultRoleAsUserWhenRegistering() {
        User user = User.register("test@example.com", "password", "홍길동");

        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals("홍길동", user.getName());
        assertEquals(UserRole.USER, user.getRole());
    }

    @Test
    @DisplayName("회원가입 시 email이 null이면 예외가 발생한다")
    void shouldThrowWhenEmailIsNullOnRegister() {
        assertThrows(NullPointerException.class, () -> User.register(null, "password", "홍길동"));
    }

    @Test
    @DisplayName("회원가입 시 password가 null이면 예외가 발생한다")
    void shouldThrowWhenPasswordIsNullOnRegister() {
        assertThrows(NullPointerException.class, () -> User.register("test@example.com", null, "홍길동"));
    }

    @Test
    @DisplayName("회원가입 시 name이 null이면 예외가 발생한다")
    void shouldThrowWhenNameIsNullOnRegister() {
        assertThrows(NullPointerException.class, () -> User.register("test@example.com", "password", null));
    }

    @Test
    @DisplayName("마지막 로그인 시각을 업데이트할 수 있다")
    void shouldUpdateLastLoginAt() {
        User user = User.register("test@example.com", "password", "홍길동");
        LocalDateTime now = LocalDateTime.of(2026, 2, 20, 22, 0);

        user.updateLastLoginAt(now);

        assertEquals(now, user.getLastLoginAt());
    }

    @Test
    @DisplayName("마지막 로그인 시각이 null이면 예외가 발생한다")
    void shouldThrowWhenLastLoginAtIsNull() {
        User user = User.register("test@example.com", "password", "홍길동");

        assertThrows(NullPointerException.class, () -> user.updateLastLoginAt(null));
    }

    @Test
    @DisplayName("UserAccessDeniedException 팩토리는 사용자 ID를 메시지에 포함한다")
    void shouldContainUserIdInAccessDeniedExceptionMessage() {
        UserAccessDeniedException exception = UserAccessDeniedException.forUser(99L);

        assertTrue(exception.getMessage().contains("99"));
    }

    @Test
    @DisplayName("본인 요청이면 접근 검증을 통과한다")
    void shouldAllowAccessForOwner() {
        User user = registeredUserWithId(10L);

        user.verifyAccessBy(10L, UserRole.USER);
    }

    @Test
    @DisplayName("ADMIN 요청이면 타 사용자도 접근 검증을 통과한다")
    void shouldAllowAccessForAdminOnOtherUser() {
        User user = registeredUserWithId(10L);

        user.verifyAccessBy(20L, UserRole.ADMIN);
    }

    @Test
    @DisplayName("타 사용자 USER 요청이면 접근 거부 예외가 발생한다")
    void shouldDenyAccessForNonOwnerUserRole() {
        User user = registeredUserWithId(10L);

        UserAccessDeniedException exception = assertThrows(
                UserAccessDeniedException.class,
                () -> user.verifyAccessBy(20L, UserRole.USER)
        );

        assertTrue(exception.getMessage().contains("10"));
    }

    @Test
    @DisplayName("접근 검증에서 요청 사용자 ID가 null이면 예외가 발생한다")
    void shouldThrowWhenRequestUserIdIsNullInAccessCheck() {
        User user = registeredUserWithId(10L);

        assertThrows(NullPointerException.class, () -> user.verifyAccessBy(null, UserRole.USER));
    }

    @Test
    @DisplayName("접근 검증에서 요청 사용자 role이 null이면 예외가 발생한다")
    void shouldThrowWhenRequestUserRoleIsNullInAccessCheck() {
        User user = registeredUserWithId(10L);

        assertThrows(NullPointerException.class, () -> user.verifyAccessBy(10L, null));
    }

    @Test
    @DisplayName("UserNotFoundException.byId 메시지에 ID가 포함된다")
    void shouldContainIdInUserNotFoundByIdMessage() {
        UserNotFoundException exception = UserNotFoundException.byId(55L);

        assertTrue(exception.getMessage().contains("55"));
    }

    @Test
    @DisplayName("UserNotFoundException.byEmail 메시지에 이메일이 포함된다")
    void shouldContainEmailInUserNotFoundByEmailMessage() {
        UserNotFoundException exception = UserNotFoundException.byEmail("x@x.com");

        assertTrue(exception.getMessage().contains("x@x.com"));
    }

    @Test
    @DisplayName("DuplicateEmailException 메시지에 이메일이 포함된다")
    void shouldContainEmailInDuplicateEmailExceptionMessage() {
        DuplicateEmailException exception = new DuplicateEmailException("dup@example.com");

        assertTrue(exception.getMessage().contains("dup@example.com"));
    }

    private User registeredUserWithId(Long id) {
        User user = User.register("test@example.com", "password", "홍길동");
        setId(user, id);
        return user;
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
