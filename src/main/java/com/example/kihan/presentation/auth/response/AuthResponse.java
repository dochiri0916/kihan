package com.example.kihan.presentation.auth.response;

import com.example.kihan.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 응답")
public record AuthResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "권한", example = "USER")
        String role,

        @Schema(description = "Access Token (Bearer)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {
    public static AuthResponse from(
            final User user,
            final String accessToken
    ) {
        return new AuthResponse(
                user.getId(),
                user.getRole().name(),
                accessToken
        );
    }
}