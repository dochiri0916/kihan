package com.dochiri.kihan.infrastructure.persistence.deadline;

import com.dochiri.kihan.domain.deadline.Deadline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeadlineJpaRepository extends JpaRepository<Deadline, Long> {

    Optional<Deadline> findByIdAndDeletedAtIsNull(Long id);

    Optional<Deadline> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    List<Deadline> findByUserIdAndDeletedAtIsNull(Long userId);

    List<Deadline> findByUserIdAndDeletedAtIsNull(Long userId, Sort sort);

    Page<Deadline> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    @Query("""
            select max(coalesce(d.updatedAt, d.createdAt))
            from Deadline d
            where d.userId = :userId
            """)
    LocalDateTime findLastModifiedAtByUserId(@Param("userId") Long userId);

    List<Deadline> findAllByDeletedAtIsNull();

}
