package com.dochiri.kihan.application.user.command;

import com.dochiri.kihan.application.user.dto.UserDetail;
import com.dochiri.kihan.domain.user.DuplicateEmailException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserService 테스트")
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserService registerUserService;

    @Test
    @DisplayName("중복 이메일이 아니면 비밀번호를 인코딩해 사용자를 저장한다")
    void shouldSaveUserWithEncodedPasswordWhenEmailIsNotDuplicate() {
        RegisterUserCommand command = new RegisterUserCommand("a@a.com", "raw", "alice");
        when(userRepository.existsByEmailAndDeletedAtIsNull(command.email())).thenReturn(false);
        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDetail result = registerUserService.execute(command);

        assertEquals("a@a.com", result.email());
        assertEquals("alice", result.name());
        assertEquals("USER", result.role());
        verify(passwordEncoder).encode("raw");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복 이메일이면 예외를 던지고 저장하지 않는다")
    void shouldThrowAndNotSaveWhenEmailIsDuplicate() {
        RegisterUserCommand command = new RegisterUserCommand("dup@a.com", "raw", "alice");
        when(userRepository.existsByEmailAndDeletedAtIsNull(command.email())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> registerUserService.execute(command));

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }
}
