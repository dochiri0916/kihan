package com.dochiri.kihan.domain.deadline;

import java.util.List;

public interface DeadlineRepository {

    Deadline save(Deadline deadline);

    Deadline findByIdAndDeletedAtIsNull(Long id);

    Deadline findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    List<Deadline> findByUserIdAndDeletedAtIsNull(Long userId);

    List<Deadline> findAllByDeletedAtIsNull();

}