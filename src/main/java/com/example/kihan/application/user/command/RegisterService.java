package com.example.kihan.application.user.command;

import com.example.kihan.application.user.dto.UserDetail;
import com.example.kihan.domain.user.DuplicateEmailException;
import com.example.kihan.domain.user.User;
import com.example.kihan.infrastructure.persistence.UserRepository;
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
    public UserDetail register(final RegisterCommand command) {
        checkDuplicateEmail(command.email());

        User user = userRepository.save(
                User.register(
                        command.email(),
                        passwordEncoder.encode(command.password()),
                        command.name()
                )
        );
        return UserDetail.from(user);
    }

    private void checkDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

}