package com.dochiri.kihan.infrastructure.persistence.execution;

import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionNotFoundException;
import com.dochiri.kihan.domain.execution.ExecutionRepository;
import com.dochiri.kihan.domain.execution.ExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaExecutionRepository implements ExecutionRepository {

    private final ExecutionJpaRepository executionJpaRepository;

    @Override
    public Execution save(Execution execution) {
        return executionJpaRepository.save(execution);
    }

    @Override
    public Execution findByIdAndDeletedAtIsNull(Long id) {
        return executionJpaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ExecutionNotFoundException(id));
    }

    @Override
    public List<Execution> findByDeadlineIdAndDeletedAtIsNull(Long deadlineId) {
        return executionJpaRepository.findByDeadlineIdAndDeletedAtIsNull(deadlineId);
    }

    @Override
    public List<Execution> findByDeadlineIdInAndScheduledDateBetweenAndDeletedAtIsNull(List<Long> deadlineIds, LocalDate startDate, LocalDate endDate) {
        return executionJpaRepository.findByDeadlineIdInAndScheduledDateBetweenAndDeletedAtIsNull(deadlineIds, startDate, endDate);
    }

    @Override
    public List<Execution> findOverdueOneTimeAndNotDone(LocalDateTime now) {
        return executionJpaRepository.findOverdueOneTimeAndNotDone(now, ExecutionStatus.DONE);
    }

    @Override
    public boolean existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(Long deadlineId, LocalDate scheduledDate) {
        return executionJpaRepository.existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(deadlineId, scheduledDate);
    }

}
