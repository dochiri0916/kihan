package com.example.pragmatic.domain.auth;

public class RefreshTokenNotFoundException extends RefreshTokenException {
    public RefreshTokenNotFoundException() {
        super("만료된 토큰입니다.");
    }
}