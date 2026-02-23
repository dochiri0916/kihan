package com.dochiri.kihan.infrastructure.persistence.execution;

import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.exception.ExecutionNotFoundException;
import com.dochiri.kihan.domain.execution.ExecutionRepository;
import com.dochiri.kihan.domain.execution.ExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaExecutionRepository implements ExecutionRepository {

    private final ExecutionJpaRepository jpaRepository;

    @Override
    public Execution save(Execution execution) {
        return jpaRepository.save(execution);
    }

    @Override
    public Execution findByIdAndDeletedAtIsNull(Long id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ExecutionNotFoundException(id));
    }

    @Override
    public Execution findByIdAndDeadlineActiveAndDeletedAtIsNull(Long id) {
        return jpaRepository.findByIdAndDeadlineActiveAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ExecutionNotFoundException(id));
    }

    @Override
    public Optional<Execution> findByDeadlineIdAndScheduledDateAndDeletedAtIsNull(Long deadlineId, LocalDate scheduledDate) {
        return jpaRepository.findByDeadlineIdAndScheduledDateAndDeletedAtIsNull(deadlineId, scheduledDate);
    }

    @Override
    public List<Execution> findByDeadlineIdAndDeletedAtIsNull(Long deadlineId) {
        return jpaRepository.findByDeadlineIdAndDeletedAtIsNull(deadlineId);
    }

    @Override
    public List<Execution> findByDeadlineIdInAndScheduledDateBetweenAndDeletedAtIsNull(List<Long> deadlineIds, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByDeadlineIdInAndScheduledDateBetweenAndDeletedAtIsNull(deadlineIds, startDate, endDate);
    }

    @Override
    public List<Execution> findOverdueOneTimeAndNotDone(LocalDate today) {
        return jpaRepository.findOverdueOneTimeAndNotDone(today, ExecutionStatus.DONE);
    }

    @Override
    public boolean existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(Long deadlineId, LocalDate scheduledDate) {
        return jpaRepository.existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(deadlineId, scheduledDate);
    }

}
