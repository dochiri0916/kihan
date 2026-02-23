package com.dochiri.kihan.domain.execution.exception;

import java.time.LocalDate;

public class ExecutionAlreadyCompletedException extends ExecutionException {

    private ExecutionAlreadyCompletedException(String message) {
        super(message);
    }

    public static ExecutionAlreadyCompletedException withDate(LocalDate scheduledDate) {
        return new ExecutionAlreadyCompletedException("이미 완료된 실행입니다: " + scheduledDate);
    }

}