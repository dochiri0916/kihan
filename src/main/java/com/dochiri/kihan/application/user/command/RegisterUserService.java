package com.dochiri.kihan.application.user.command;

import com.dochiri.kihan.application.user.dto.UserDetail;
import com.dochiri.kihan.domain.user.DuplicateEmailException;
import com.dochiri.kihan.domain.user.User;
import com.dochiri.kihan.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDetail execute(RegisterUserCommand command) {
        checkDuplicateEmail(command.email());

        User user;
        try {
            user = userRepository.save(
                    User.register(
                            command.email(),
                            passwordEncoder.encode(command.password()),
                            command.name()
                    )
            );
        } catch (DataIntegrityViolationException exception) {
            // Handle race condition: duplicate request can pass exists-check and fail on DB unique key.
            throw new DuplicateEmailException(command.email());
        }

        return UserDetail.from(user);
    }

    private void checkDuplicateEmail(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new DuplicateEmailException(email);
        }
    }

}
