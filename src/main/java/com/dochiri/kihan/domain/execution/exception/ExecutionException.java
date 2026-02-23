package com.dochiri.kihan.domain.execution.exception;

public abstract class ExecutionException extends RuntimeException {

    protected ExecutionException(String message) {
        super(message);
    }

}