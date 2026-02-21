package com.dochiri.kihan.presentation.auth.response;

import com.dochiri.kihan.application.user.dto.UserDetail;

public record AuthResponse(
        Long userId,
        String role,
    String accessToken,
        String refreshToken
) {
    public static AuthResponse from(
            UserDetail user,
            String accessToken,
            String refreshToken
    ) {
        return new AuthResponse(
                user.id(),
                user.role(),
                accessToken,
                refreshToken
        );
    }
}
