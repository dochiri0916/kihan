package com.dochiri.kihan.application.execution.query;

import com.dochiri.kihan.application.deadline.dto.DeadlineDetail;
import com.dochiri.kihan.application.deadline.query.DeadlineQueryService;
import com.dochiri.kihan.application.execution.dto.ExecutionDetail;
import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExecutionQueryService {

    private final ExecutionRepository executionRepository;
    private final DeadlineQueryService deadlineQueryService;

    public List<ExecutionDetail> findByDeadlineId(Long userId, Long deadlineId) {
        deadlineQueryService.getById(userId, deadlineId);
        return executionRepository.findByDeadlineIdAndDeletedAtIsNull(deadlineId).stream()
                .map(ExecutionDetail::from)
                .toList();
    }

    public List<ExecutionDetail> findByDateRange(DateRangeQuery query) {
        List<Long> userDeadlineIds = deadlineQueryService.getAllByUserId(query.userId()).stream()
                .map(DeadlineDetail::id)
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

    public ExecutionDetail findById(Long userId, Long executionId) {
        Execution execution = executionRepository.findByIdAndDeletedAtIsNull(executionId);

        if (execution.getDeadline().isDeleted()) {
            throw new com.dochiri.kihan.domain.execution.ExecutionNotFoundException(executionId);
        }
        execution.getDeadline().verifyOwnership(userId);
        return ExecutionDetail.from(execution);
    }

}
