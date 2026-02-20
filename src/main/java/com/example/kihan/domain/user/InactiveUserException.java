package com.example.kihan.domain.user;

public class InactiveUserException extends UserException {
    public InactiveUserException() {
        super("비활성화된 계정입니다.");
    }
}