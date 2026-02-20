package com.example.kihan.application.deadline.command;

import com.example.kihan.application.deadline.dto.UpdateDeadlineCommand;
import com.example.kihan.application.deadline.query.DeadlineLoader;
import com.example.kihan.domain.deadline.Deadline;
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
    public void update(final UpdateDeadlineCommand command) {
        Deadline deadline = deadlineLoader.loadByIdAndUserId(command.deadlineId(), command.userId());
        deadline.update(command.title(), command.description());
    }

    public void markAsCompleted(final Long userId, final Long deadlineId) {
        Deadline deadline = deadlineLoader.loadByIdAndUserId(deadlineId, userId);
        deadline.markAsCompleted(LocalDateTime.now(clock));
    }

}
