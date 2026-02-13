package com.example.kihan.application.user.query;

import com.example.kihan.application.user.dto.UserDetail;
import com.example.kihan.domain.user.User;
import com.example.kihan.domain.user.UserNotFoundException;
import com.example.kihan.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService implements UserFinder, UserLoader {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findActiveUserById(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Optional<User> findActiveUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User loadActiveUserById(Long id) {
        return findActiveUserById(id)
                .orElseThrow(() -> UserNotFoundException.withId(id));
    }

    @Override
    public User loadActiveUserByEmail(String email) {
        return findActiveUserByEmail(email)
                .orElseThrow(() -> UserNotFoundException.withEmail(email));
    }

    @Override
    public User loadByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.withEmail(email));
    }

    public UserDetail getActiveUser(Long userId) {
        User user = loadActiveUserById(userId);
        return UserDetail.from(user);
    }

}