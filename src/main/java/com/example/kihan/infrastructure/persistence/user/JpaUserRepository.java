package com.example.kihan.infrastructure.persistence.user;

import com.example.kihan.domain.user.User;
import com.example.kihan.domain.user.UserNotFoundException;
import com.example.kihan.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public User findByIdAndDeletedAtIsNull(Long id) {
        return userJpaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> UserNotFoundException.byId(id));
    }

    @Override
    public User findByEmailAndDeletedAtIsNull(String email) {
        return userJpaRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> UserNotFoundException.byEmail(email));
    }

    @Override
    public boolean existsByEmailAndDeletedAtIsNull(String email) {
        return userJpaRepository.existsByEmailAndDeletedAtIsNull(email);
    }

}