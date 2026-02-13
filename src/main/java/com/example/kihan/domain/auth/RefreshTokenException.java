package com.example.kihan.domain.auth;

public abstract class RefreshTokenException extends RuntimeException {
    protected RefreshTokenException(String message) {
        super(message);
    }
}