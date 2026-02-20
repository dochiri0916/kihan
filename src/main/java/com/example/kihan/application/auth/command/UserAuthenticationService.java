package com.example.kihan.application.auth.command;

import com.example.kihan.domain.auth.InvalidCredentialsException;
import com.example.kihan.domain.user.User;
import com.example.kihan.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User execute(LoginCommand command) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(command.email());

        validatePassword(command.password(), user.getPassword());

        return user;
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new InvalidCredentialsException();
        }
    }

}