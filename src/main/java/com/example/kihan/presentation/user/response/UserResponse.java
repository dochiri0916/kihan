package com.example.kihan.presentation.user.response;

import com.example.kihan.domain.user.User;

public record UserResponse(
        Long id,
        String email,
        String name,
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