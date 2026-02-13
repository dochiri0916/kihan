package com.example.kihan.presentation.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record RegisterRequest(
        @Schema(description = "이메일", example = "user@example.com")
        @Email
        @NotBlank
        String email,

        @Schema(description = "비밀번호 (8-20자)", example = "password123")
        @Size(min = 8, max = 20)
        @NotBlank
        String password,

        @Schema(description = "이름 (2-10자)", example = "홍길동")
        @Size(min = 2, max = 10)
        @NotBlank
        String name
) {
}