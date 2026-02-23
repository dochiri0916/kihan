package com.dochiri.kihan.domain.user.exception;

public class UserNotFoundException extends UserException {
    private UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException byId(Long userId) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId);
    }

    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다: " + email);
    }
}