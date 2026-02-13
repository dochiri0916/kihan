package com.example.pragmatic.application.user.query;

import com.example.pragmatic.domain.user.User;

public interface UserFinder {

    User findActiveUserById(Long id);

    User findActiveUserByEmail(String email);

    User findByEmail(String email);

}