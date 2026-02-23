package com.dochiri.kihan.domain.deadline.exception;

public abstract class DeadlineException extends RuntimeException {
    protected DeadlineException(String message) {
        super(message);
    }
}