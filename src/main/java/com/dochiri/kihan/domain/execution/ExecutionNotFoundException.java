package com.dochiri.kihan.domain.execution;

public class ExecutionNotFoundException extends ExecutionException {

    private ExecutionNotFoundException(String message) {
        super(message);
    }

    public static ExecutionNotFoundException withId(Long executionId) {
        return new ExecutionNotFoundException("실행을 찾을 수 없습니다: " + executionId);
    }

}
