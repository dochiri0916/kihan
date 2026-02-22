package com.dochiri.kihan.domain.execution;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExecutionRepository {

    Execution save(Execution execution);

    Execution findByIdAndDeletedAtIsNull(Long id);
    Execution findByIdAndDeadlineActiveAndDeletedAtIsNull(Long id);

    Optional<Execution> findByDeadlineIdAndScheduledDateAndDeletedAtIsNull(Long deadlineId, LocalDate scheduledDate);

    List<Execution> findByDeadlineIdAndDeletedAtIsNull(Long deadlineId);

    List<Execution> findByDeadlineIdInAndScheduledDateBetweenAndDeletedAtIsNull(
            List<Long> deadlineIds,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Execution> findOverdueOneTimeAndNotDone(LocalDate today);

    boolean existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(Long deadlineId, LocalDate scheduledDate);

}
