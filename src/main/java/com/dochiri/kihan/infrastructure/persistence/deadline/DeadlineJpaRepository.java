package com.dochiri.kihan.infrastructure.persistence.deadline;

import com.dochiri.kihan.domain.deadline.Deadline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeadlineJpaRepository extends JpaRepository<Deadline, Long> {
    Optional<Deadline> findByIdAndDeletedAtIsNull(Long id);
    Optional<Deadline> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
    List<Deadline> findByUserIdAndDeletedAtIsNull(Long userId);
    List<Deadline> findAllByDeletedAtIsNull();
}
