package com.example.kihan.application.user.query;

import com.example.kihan.domain.user.User;

public interface UserLoader {

    User loadActiveUserById(Long id);

    User loadActiveUserByEmail(String email);

    User loadByEmail(String email);

}
