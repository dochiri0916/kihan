package com.dochiri.kihan.application.deadline.command;

import com.dochiri.kihan.application.deadline.query.DeadlineLoader;
import com.dochiri.kihan.domain.deadline.Deadline;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeleteDeadlineService {

    private final DeadlineLoader deadlineLoader;
    private final Clock clock;

    @Transactional
    public void execute(Long userId, Long deadlineId) {
        Deadline deadline = deadlineLoader.loadByIdAndUserId(deadlineId, userId);
        deadline.delete(LocalDateTime.now(clock));
    }

}
