package com.example.kihan.domain.deadline;

import java.time.LocalDate;

public class ExecutionAlreadyCompletedException extends ExecutionException {

    private ExecutionAlreadyCompletedException(final String message) {
        super(message);
    }

    public static ExecutionAlreadyCompletedException withDate(final LocalDate scheduledDate) {
        return new ExecutionAlreadyCompletedException("이미 완료된 실행입니다: " + scheduledDate);
    }

}
