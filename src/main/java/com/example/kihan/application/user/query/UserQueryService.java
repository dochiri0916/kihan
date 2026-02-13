package com.example.kihan.application.user.query;

import com.example.kihan.domain.user.User;
import com.example.kihan.domain.user.UserNotFoundException;
import com.example.kihan.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService implements UserFinder {

    private final UserRepository userRepository;

    @Override
    public User findActiveUserById(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User findActiveUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public User getActiveUser(Long id) {
        return findActiveUserById(id);
    }

}