package com.dochiri.kihan.infrastructure.persistence.execution;

import com.dochiri.kihan.domain.execution.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExecutionJpaRepository extends JpaRepository<Execution, Long> {

    List<Execution> findByDeadlineIdAndDeletedAtIsNull(Long deadlineId);

    List<Execution> findByDeadlineIdInAndDeletedAtIsNull(List<Long> deadlineIds);

    List<Execution> findByDeadlineIdInAndScheduledDateBetweenAndDeletedAtIsNull(
            List<Long> deadlineIds,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Execution> findByScheduledDateBetweenAndDeletedAtIsNull(LocalDate startDate, LocalDate endDate);

    Optional<Execution> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(Long deadlineId, LocalDate scheduledDate);

}
