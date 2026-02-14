package com.example.kihan.infrastructure.persistence;

import com.example.kihan.domain.deadline.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {

    List<Execution> findByDeadlineIdAndDeletedAtIsNull(Long deadlineId);

    List<Execution> findByDeadlineIdInAndDeletedAtIsNull(List<Long> deadlineIds);

    List<Execution> findByScheduledDateBetweenAndDeletedAtIsNull(LocalDate startDate, LocalDate endDate);

    Optional<Execution> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(Long deadlineId, LocalDate scheduledDate);

}
