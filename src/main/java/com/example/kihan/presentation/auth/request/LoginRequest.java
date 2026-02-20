package com.example.kihan.presentation.auth.request;

import com.example.kihan.application.auth.command.LoginCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Email
        String email,

        @Size(min = 8, max = 20)
        @NotBlank
        String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}