package com.example.kihan.application.deadline.query;

import com.example.kihan.domain.deadline.Deadline;

public interface DeadlineLoader {

    Deadline loadByIdAndUserId(Long deadlineId, Long userId);

}