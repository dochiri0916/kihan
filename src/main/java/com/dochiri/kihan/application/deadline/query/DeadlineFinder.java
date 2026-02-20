package com.dochiri.kihan.application.deadline.query;

import com.dochiri.kihan.domain.deadline.Deadline;

import java.util.Optional;

public interface DeadlineFinder {

    Optional<Deadline> findByIdAndUserId(Long deadlineId, Long userId);

}
