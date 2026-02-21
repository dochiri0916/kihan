package com.dochiri.kihan.infrastructure.persistence.execution;

import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select e
            from Execution e
            join fetch e.deadline d
            where e.deletedAt is null
              and d.deletedAt is null
              and d.type = com.dochiri.kihan.domain.deadline.DeadlineType.ONE_TIME
              and d.dueDate < :now
              and e.status <> :doneStatus
            """)
    List<Execution> findOverdueOneTimeAndNotDone(
            @Param("now") LocalDate now,
            @Param("doneStatus") ExecutionStatus doneStatus
    );

}
