package com.dochiri.kihan.domain.user;

public abstract class UserException extends RuntimeException {
    protected UserException(String message) {
        super(message);
    }
}