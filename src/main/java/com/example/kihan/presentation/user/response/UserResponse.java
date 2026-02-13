package com.example.kihan.presentation.user.response;

import com.example.kihan.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 응답")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "권한", example = "USER")
        String role
) {
    public static UserResponse from(final User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );
    }
}