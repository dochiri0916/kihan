package com.example.kihan.presentation.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email
        @NotBlank
        String email,

        @Size(min = 8, max = 20)
        @NotBlank
        String password,

        @Size(min = 2, max = 10)
        @NotBlank
        String name
) {
}