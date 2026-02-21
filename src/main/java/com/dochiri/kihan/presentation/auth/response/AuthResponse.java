package com.dochiri.kihan.presentation.auth.response;

import com.dochiri.kihan.domain.user.User;

public record AuthResponse(
        Long userId,
        String role,
        String accessToken,
        String refreshToken
) {
    public static AuthResponse from(
            User user,
            String accessToken,
            String refreshToken
    ) {
        return new AuthResponse(
                user.getId(),
                user.getRole().name(),
                accessToken,
                refreshToken
        );
    }
}
