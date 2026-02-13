package com.example.pragmatic.application.user.command;

import com.example.pragmatic.domain.user.DuplicateEmailException;
import com.example.pragmatic.domain.user.User;
import com.example.pragmatic.infrastructure.persistence.UserRepository;
import com.example.pragmatic.presentation.user.request.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(final RegisterRequest request) {
        checkDuplicateEmail(request.email());

        return userRepository.save(
                User.register(
                        request.email(),
                        passwordEncoder.encode(request.password()),
                        request.name()
                )
        );
    }

    private void checkDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

}