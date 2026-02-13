package com.example.kihan.application.user.query;

import com.example.kihan.domain.user.User;

import java.util.Optional;

public interface UserFinder {

    Optional<User> findActiveUserById(Long id);

    Optional<User> findActiveUserByEmail(String email);

    Optional<User> findByEmail(String email);

}