package com.dochiri.kihan.domain.execution;

public abstract class ExecutionException extends RuntimeException {

    protected ExecutionException(String message) {
        super(message);
    }

}