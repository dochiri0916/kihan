package com.example.kihan.application.execution.query;

import com.example.kihan.application.execution.dto.ExecutionDetail;
import com.example.kihan.domain.deadline.Execution;
import com.example.kihan.domain.deadline.ExecutionNotFoundException;
import com.example.kihan.infrastructure.persistence.DeadlineRepository;
import com.example.kihan.infrastructure.persistence.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExecutionQueryService {

    private final ExecutionRepository executionRepository;
    private final DeadlineRepository deadlineRepository;

    public List<ExecutionDetail> findByDeadlineId(final Long userId, final Long deadlineId) {
        List<Long> userDeadlineIds = deadlineRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
                .map(deadline -> deadline.getId())
                .toList();

        if (!userDeadlineIds.contains(deadlineId)) {
            return List.of();
        }

        return executionRepository.findByDeadlineIdAndDeletedAtIsNull(deadlineId).stream()
                .map(ExecutionDetail::from)
                .toList();
    }

    public List<ExecutionDetail> findByDateRange(final DateRangeQuery query) {
        List<Long> userDeadlineIds = deadlineRepository.findByUserIdAndDeletedAtIsNull(query.userId()).stream()
                .map(deadline -> deadline.getId())
                .toList();

        if (userDeadlineIds.isEmpty()) {
            return List.of();
        }

        return executionRepository.findByDeadlineIdInAndDeletedAtIsNull(userDeadlineIds).stream()
                .filter(execution -> !execution.getScheduledDate().isBefore(query.startDate())
                        && !execution.getScheduledDate().isAfter(query.endDate()))
                .map(ExecutionDetail::from)
                .toList();
    }

    public ExecutionDetail findById(final Long userId, final Long executionId) {
        Execution execution = executionRepository.findByIdAndDeletedAtIsNull(executionId)
                .orElseThrow(() -> ExecutionNotFoundException.withId(executionId));

        execution.getDeadline().verifyOwnership(userId);
        return ExecutionDetail.from(execution);
    }

}
