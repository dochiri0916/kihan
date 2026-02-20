package com.example.kihan.application.auth.dto;

import com.example.kihan.domain.user.User;

public record LoginResult(
        User user,
        String accessToken,
        String refreshToken
) {
    public static LoginResult from(User user, String accessToken, String refreshToken) {
        return new LoginResult(user, accessToken, refreshToken);
    }
}