package com.example.pragmatic.infrastructure.persistence;

import com.example.pragmatic.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

}