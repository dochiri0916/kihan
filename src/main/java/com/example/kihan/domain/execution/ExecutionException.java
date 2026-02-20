package com.example.kihan.domain.execution;

public abstract class ExecutionException extends RuntimeException {

    protected ExecutionException(final String message) {
        super(message);
    }

}
