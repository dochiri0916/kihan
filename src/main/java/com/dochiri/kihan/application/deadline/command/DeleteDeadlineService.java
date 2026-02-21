package com.dochiri.kihan.application.deadline.command;

import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.deadline.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeleteDeadlineService {

    private final DeadlineRepository deadlineRepository;
    private final Clock clock;

    @Transactional
    public void execute(Long userId, Long id) {
        Deadline deadline = deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId);
        deadline.delete(LocalDateTime.now(clock));
    }

}
