package com.example.kihan.infrastructure.persistence;

import com.example.kihan.domain.deadline.Deadline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeadlineRepository extends JpaRepository<Deadline, Long> {
    List<Deadline> findByUserIdAndDeletedAtIsNull(Long userId);
    Optional<Deadline> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
