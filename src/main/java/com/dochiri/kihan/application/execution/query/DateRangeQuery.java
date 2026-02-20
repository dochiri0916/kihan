package com.dochiri.kihan.application.execution.query;

import java.time.LocalDate;

public record DateRangeQuery(
        Long userId,
        LocalDate startDate,
        LocalDate endDate
) {
}
