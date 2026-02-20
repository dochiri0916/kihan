package com.example.kihan.domain.execution;

public class ExecutionNotFoundException extends ExecutionException {

    private ExecutionNotFoundException(final String message) {
        super(message);
    }

    public static ExecutionNotFoundException withId(final Long executionId) {
        return new ExecutionNotFoundException("실행을 찾을 수 없습니다: " + executionId);
    }

}
