package com.dochiri.kihan.domain.auth;

public class InvalidCredentialsException extends AuthenticationException {
    public InvalidCredentialsException() {
        super("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}