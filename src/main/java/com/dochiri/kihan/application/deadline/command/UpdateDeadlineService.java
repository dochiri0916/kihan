package com.dochiri.kihan.application.deadline.command;

import com.dochiri.kihan.application.deadline.dto.UpdateDeadlineCommand;
import com.dochiri.kihan.application.deadline.query.DeadlineLoader;
import com.dochiri.kihan.domain.deadline.Deadline;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateDeadlineService {

    private final DeadlineLoader deadlineLoader;
    private final Clock clock;

    @Transactional
    public void update(UpdateDeadlineCommand command) {
        Deadline deadline = deadlineLoader.loadByIdAndUserId(command.deadlineId(), command.userId());
        deadline.update(command.title(), command.description());
    }

    public void markAsCompleted(Long userId, Long deadlineId) {
        Deadline deadline = deadlineLoader.loadByIdAndUserId(deadlineId, userId);
        deadline.markAsCompleted(LocalDateTime.now(clock));
    }

}
