package com.example.kihan.application.execution.query;

import com.example.kihan.application.execution.dto.ExecutionDetail;
import com.example.kihan.domain.deadline.Execution;
import com.example.kihan.domain.deadline.ExecutionNotFoundException;
import com.example.kihan.infrastructure.persistence.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExecutionQueryService {

    private final ExecutionRepository executionRepository;

    public List<ExecutionDetail> findByDeadlineId(final Long deadlineId) {
        return executionRepository.findByDeadlineIdAndDeletedAtIsNull(deadlineId).stream()
                .map(ExecutionDetail::from)
                .toList();
    }

    public List<ExecutionDetail> findByDateRange(final LocalDate startDate, final LocalDate endDate) {
        return executionRepository.findByScheduledDateBetweenAndDeletedAtIsNull(startDate, endDate).stream()
                .map(ExecutionDetail::from)
                .toList();
    }

    public ExecutionDetail findById(final Long executionId) {
        Execution execution = executionRepository.findByIdAndDeletedAtIsNull(executionId)
                .orElseThrow(() -> ExecutionNotFoundException.withId(executionId));

        return ExecutionDetail.from(execution);
    }

}
