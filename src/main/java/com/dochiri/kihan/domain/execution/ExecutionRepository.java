package com.dochiri.kihan.domain.execution;

import java.time.LocalDate;
import java.util.List;

public interface ExecutionRepository {

    Execution save(Execution execution);

    Execution findByIdAndDeletedAtIsNull(Long id);

    List<Execution> findByDeadlineIdAndDeletedAtIsNull(Long deadlineId);

    List<Execution> findByDeadlineIdInAndScheduledDateBetweenAndDeletedAtIsNull(
            List<Long> deadlineIds,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Execution> findOverdueOneTimeAndNotDone(LocalDate today);

    boolean existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(Long deadlineId, LocalDate scheduledDate);

}
