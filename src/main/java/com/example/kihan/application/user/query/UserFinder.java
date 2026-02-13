package com.example.kihan.application.user.query;

import com.example.kihan.domain.user.User;

public interface UserFinder {

    User findActiveUserById(Long id);

    User findActiveUserByEmail(String email);

    User findByEmail(String email);

}