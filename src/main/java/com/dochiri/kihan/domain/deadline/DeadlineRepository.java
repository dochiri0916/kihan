package com.dochiri.kihan.domain.deadline;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface DeadlineRepository {

    Deadline save(Deadline deadline);

    Deadline findByIdAndDeletedAtIsNull(Long id);

    Deadline findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    List<Deadline> findByUserIdAndDeletedAtIsNull(Long userId);

    List<Deadline> findByUserIdAndDeletedAtIsNull(Long userId, Sort sort);

    Page<Deadline> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    LocalDateTime findLastModifiedAtByUserId(Long userId);

    List<Deadline> findAllByDeletedAtIsNull();

}
