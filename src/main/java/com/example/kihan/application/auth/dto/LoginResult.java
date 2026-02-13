package com.example.pragmatic.application.auth.dto;

import com.example.pragmatic.domain.user.User;

public record LoginResult(
        User user,
        String accessToken,
        String refreshToken
) {
}