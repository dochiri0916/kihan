package com.example.kihan.application.execution.query;

import java.time.LocalDate;

public record DateRangeQuery(
        Long userId,
        LocalDate startDate,
        LocalDate endDate
) {
}
