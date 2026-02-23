package com.dochiri.kihan.domain.execution.exception;

public class InvalidExecutionStatusTransitionException extends ExecutionException {

    private InvalidExecutionStatusTransitionException(String message) {
        super(message);
    }

    public static InvalidExecutionStatusTransitionException cannotPauseWhenAlreadyPaused() {
        return new InvalidExecutionStatusTransitionException("이미 중지된 실행입니다.");
    }

    public static InvalidExecutionStatusTransitionException cannotResumeWhenNotPaused() {
        return new InvalidExecutionStatusTransitionException("중지 상태에서만 재개할 수 있습니다.");
    }
}
