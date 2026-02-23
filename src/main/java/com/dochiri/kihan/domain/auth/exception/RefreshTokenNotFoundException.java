package com.dochiri.kihan.domain.auth.exception;

public class RefreshTokenNotFoundException extends RefreshTokenException {
    public RefreshTokenNotFoundException() {
        super("리프레시 토큰을 찾을 수 없습니다.");
    }
}
