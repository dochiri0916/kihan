package com.dochiri.kihan.application.execution.usecase;

import com.dochiri.kihan.application.execution.dto.ExecutionDetail;
import com.dochiri.kihan.application.execution.query.DateRangeQuery;
import com.dochiri.kihan.application.execution.query.ExecutionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetExecutionsByDateRangeUseCase {

    private final ExecutionQueryService executionQueryService;

    public List<ExecutionDetail> execute(Long userId, LocalDate startDate, LocalDate endDate) {
        return executionQueryService.getByDateRange(new DateRangeQuery(userId, startDate, endDate));
    }
}
