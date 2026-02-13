package com.example.pragmatic.domain.auth;

public abstract class RefreshTokenException extends RuntimeException {

    protected RefreshTokenException(String message) {
        super(message);
    }

}