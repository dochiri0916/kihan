package com.example.pragmatic.application.auth.command;

import com.example.pragmatic.application.user.query.UserFinder;
import com.example.pragmatic.domain.auth.InvalidCredentialsException;
import com.example.pragmatic.domain.user.User;
import com.example.pragmatic.domain.user.UserNotActiveException;
import com.example.pragmatic.presentation.auth.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthenticationService {

    private final UserFinder userFinder;
    private final PasswordEncoder passwordEncoder;

    public User authenticate(final LoginRequest request) {
        User user = userFinder.findByEmail(request.email());

        if (!user.isActive()) {
            throw new UserNotActiveException();
        }

        validatePassword(request.password(), user.getPassword());

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