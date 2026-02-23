package com.dochiri.kihan.application.auth.command;

import com.dochiri.kihan.domain.auth.exception.InvalidCredentialsException;
import com.dochiri.kihan.domain.user.User;
import com.dochiri.kihan.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAuthenticationService 테스트")
class UserAuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAuthenticationService userAuthenticationService;

    @Test
    @DisplayName("비밀번호가 일치하면 사용자를 반환한다")
    void shouldReturnUserWhenPasswordMatches() {
        LoginCommand command = new LoginCommand("a@a.com", "raw");
        User user = User.register("a@a.com", "encoded", "alice");
        when(userRepository.findByEmailAndDeletedAtIsNull("a@a.com")).thenReturn(user);
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

        User result = userAuthenticationService.execute(command);

        assertEquals(user, result);
        verify(passwordEncoder).matches("raw", "encoded");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외를 던진다")
    void shouldThrowWhenPasswordDoesNotMatch() {
        LoginCommand command = new LoginCommand("a@a.com", "wrong");
        User user = User.register("a@a.com", "encoded", "alice");
        when(userRepository.findByEmailAndDeletedAtIsNull("a@a.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userAuthenticationService.execute(command));
    }
}
