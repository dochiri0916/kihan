package com.example.kihan.application.auth.command;

import com.example.kihan.application.user.query.UserLoader;
import com.example.kihan.domain.auth.InvalidCredentialsException;
import com.example.kihan.domain.user.User;
import com.example.kihan.domain.user.InactiveUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthenticationService {

    private final UserLoader userLoader;
    private final PasswordEncoder passwordEncoder;

    public User authenticate(final LoginCommand command) {
        User user = userLoader.loadByEmail(command.email());

        if (user.isDeleted()) {
            throw new InactiveUserException();
        }

        validatePassword(command.password(), user.getPassword());

        return user;
    }

    private void validatePassword(
            final String rawPassword,
            final String encodedPassword
    ) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new InvalidCredentialsException();
        }
    }

}