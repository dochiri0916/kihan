package com.dochiri.kihan.domain.auth;

public class ExpiredRefreshTokenException extends RefreshTokenException {
    public ExpiredRefreshTokenException() {
        super("만료된 리프레시 토큰입니다.");
    }
}