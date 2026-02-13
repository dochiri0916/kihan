package com.example.kihan.domain.user;

public class DuplicateEmailException extends UserException {
    public DuplicateEmailException(String email) {
        super("이미 사용중인 이메일입니다: " + email);
    }
}