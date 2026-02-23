package com.dochiri.kihan.domain.auth.exception;

public abstract class RefreshTokenException extends RuntimeException {
    protected RefreshTokenException(String message) {
        super(message);
    }
}