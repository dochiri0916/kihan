package com.example.kihan.application.execution.query;

import com.example.kihan.application.execution.dto.ExecutionDetail;
import com.example.kihan.domain.execution.Execution;
import com.example.kihan.domain.execution.ExecutionNotFoundException;
import com.example.kihan.infrastructure.persistence.DeadlineRepository;
import com.example.kihan.infrastructure.persistence.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExecutionQueryService implements ExecutionFinder, ExecutionLoader {

    private final ExecutionRepository executionRepository;
    private final DeadlineRepository deadlineRepository;

    @Override
    public Optional<Execution> findActiveById(final Long executionId) {
        return executionRepository.findByIdAndDeletedAtIsNull(executionId);
    }

    @Override
    public Execution loadActiveById(final Long executionId) {
        return findActiveById(executionId)
                .orElseThrow(() -> ExecutionNotFoundException.withId(executionId));
    }

    public List<ExecutionDetail> findByDeadlineId(final Long userId, final Long deadlineId) {
        if (deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(deadlineId, userId).isEmpty()) {
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

        return executionRepository.findByDeadlineIdInAndScheduledDateBetweenAndDeletedAtIsNull(
                        userDeadlineIds,
                        query.startDate(),
                        query.endDate()
                ).stream()
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
