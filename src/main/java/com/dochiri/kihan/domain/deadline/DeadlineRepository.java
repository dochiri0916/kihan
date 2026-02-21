package com.dochiri.kihan.domain.deadline;

import java.util.List;
import org.springframework.data.domain.Sort;

public interface DeadlineRepository {

    Deadline save(Deadline deadline);

    Deadline findByIdAndDeletedAtIsNull(Long id);

    Deadline findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    List<Deadline> findByUserIdAndDeletedAtIsNull(Long userId);

    List<Deadline> findByUserIdAndDeletedAtIsNull(Long userId, Sort sort);

    List<Deadline> findAllByDeletedAtIsNull();

}
