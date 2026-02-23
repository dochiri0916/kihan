package com.dochiri.kihan.infrastructure.persistence.user;

import com.dochiri.kihan.domain.user.User;
import com.dochiri.kihan.domain.user.exception.UserNotFoundException;
import com.dochiri.kihan.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public User findByIdAndDeletedAtIsNull(Long id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> UserNotFoundException.byId(id));
    }

    @Override
    public User findByEmailAndDeletedAtIsNull(String email) {
        return jpaRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> UserNotFoundException.byEmail(email));
    }

    @Override
    public boolean existsByEmailAndDeletedAtIsNull(String email) {
        return jpaRepository.existsByEmailAndDeletedAtIsNull(email);
    }

}