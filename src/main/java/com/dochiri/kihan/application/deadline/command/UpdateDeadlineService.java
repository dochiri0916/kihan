package com.dochiri.kihan.application.deadline.command;

import com.dochiri.kihan.application.deadline.dto.UpdateDeadlineCommand;
import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateDeadlineService {

    private final DeadlineRepository deadlineRepository;
    private final Clock clock;

    @Transactional
    public void update(UpdateDeadlineCommand command) {
        Deadline deadline = deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(command.deadlineId(), command.userId());
        deadline.update(command.title(), command.description());
    }

    @Transactional
    public void markAsCompleted(Long userId, Long deadlineId) {
        Deadline deadline = deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(deadlineId, userId);
        deadline.markAsCompleted(LocalDateTime.now(clock));
    }

}
