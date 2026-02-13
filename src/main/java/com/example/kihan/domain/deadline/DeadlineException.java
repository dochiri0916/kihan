package com.example.kihan.domain.deadline;

public abstract class DeadlineException extends RuntimeException {
    protected DeadlineException(String message) {
        super(message);
    }
}