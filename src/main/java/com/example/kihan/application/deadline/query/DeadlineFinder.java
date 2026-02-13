package com.example.kihan.application.deadline.query;

import com.example.kihan.domain.deadline.Deadline;

import java.util.Optional;

public interface DeadlineFinder {

    Optional<Deadline> findByIdAndUserId(Long deadlineId, Long userId);

}
