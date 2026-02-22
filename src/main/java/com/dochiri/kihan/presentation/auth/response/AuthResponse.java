package com.dochiri.kihan.presentation.auth.response;

import com.dochiri.kihan.application.user.dto.UserDetail;

public record AuthResponse(
        Long userId,
        String role,
        String accessToken
) {
    public static AuthResponse from(
            UserDetail user,
            String accessToken
    ) {
        return new AuthResponse(
                user.id(),
                user.role(),
                accessToken
        );
    }
}
