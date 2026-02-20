package com.dochiri.kihan.application.deadline.query;

import com.dochiri.kihan.domain.deadline.Deadline;

public interface DeadlineLoader {

    Deadline loadByIdAndUserId(Long deadlineId, Long userId);

}