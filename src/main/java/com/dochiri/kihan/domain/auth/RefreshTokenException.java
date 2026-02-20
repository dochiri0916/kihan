package com.dochiri.kihan.domain.auth;

public abstract class RefreshTokenException extends RuntimeException {
    protected RefreshTokenException(String message) {
        super(message);
    }
}