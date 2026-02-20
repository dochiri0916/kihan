package com.dochiri.kihan.presentation.user.response;

import com.dochiri.kihan.application.user.dto.UserDetail;
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
    public static UserResponse from(UserDetail detail) {
        return new UserResponse(
                detail.id(),
                detail.email(),
                detail.name(),
                detail.role()
        );
    }
}