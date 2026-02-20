package com.example.kihan.presentation.auth.response;

import com.example.kihan.domain.user.User;

public record AuthResponse(
        Long userId,
        String role,
        String accessToken
) {
    public static AuthResponse from(
            User user,
            String accessToken
    ) {
        return new AuthResponse(
                user.getId(),
                user.getRole().name(),
                accessToken
        );
    }
}
