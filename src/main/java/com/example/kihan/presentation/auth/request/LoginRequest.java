package com.example.kihan.presentation.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "이메일", example = "user@example.com")
        @Email
        String email,

        @Schema(description = "비밀번호 (8-20자)", example = "password123")
        @Size(min = 8, max = 20)
        @NotBlank
        String password
) {
}