package com.dochiri.kihan.domain.execution;

public class ExecutionNotFoundException extends ExecutionException {

    public ExecutionNotFoundException(Long id) {
        super("실행을 찾을 수 없습니다: " + id);
    }

}