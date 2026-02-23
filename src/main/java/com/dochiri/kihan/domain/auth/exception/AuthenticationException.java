package com.dochiri.kihan.domain.auth.exception;

public abstract class AuthenticationException extends RuntimeException {
    protected AuthenticationException(String message) {
        super(message);
    }
}