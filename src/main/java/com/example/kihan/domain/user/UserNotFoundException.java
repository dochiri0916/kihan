package com.example.kihan.domain.user;

public class UserNotFoundException extends UserException {
    public UserNotFoundException() {
        super("사용자를 찾을 수 없습니다.");
    }

    public UserNotFoundException(Long userId) {
        super("사용자를 찾을 수 없습니다: " + userId);
    }

    public UserNotFoundException(String email) {
        super("사용자를 찾을 수 없습니다: " + email);
    }
}