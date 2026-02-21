package com.dochiri.kihan.domain.execution;

import java.time.LocalDate;

public class InvalidExecutionDateRangeException extends ExecutionException {
    private InvalidExecutionDateRangeException(String message) {
        super(message);
    }

    public static InvalidExecutionDateRangeException startDateAfterEndDate(LocalDate startDate, LocalDate endDate) {
        return new InvalidExecutionDateRangeException(
                "startDate는 endDate보다 늦을 수 없습니다: startDate=%s, endDate=%s"
                        .formatted(startDate, endDate)
        );
    }
}
