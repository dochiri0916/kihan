package com.dochiri.kihan.application.execution.query;

import com.dochiri.kihan.domain.execution.InvalidExecutionDateRangeException;

import java.time.LocalDate;

public record DateRangeQuery(
        Long userId,
        LocalDate startDate,
        LocalDate endDate
) {
    public DateRangeQuery {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw InvalidExecutionDateRangeException.startDateAfterEndDate(startDate, endDate);
        }
    }
}
