package com.example.kihan.application.deadline.command;

import com.example.kihan.application.deadline.query.DeadlineLoader;
import com.example.kihan.domain.deadline.Deadline;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeadlineUpdateService {

    private final DeadlineLoader deadlineLoader;

    public void changeTitle(Long userId, Long deadlineId, String newTitle) {
        Deadline deadline = deadlineLoader.loadByIdAndUserId(deadlineId, userId);
        deadline.changeTitle(newTitle);
    }

    public void changeDescription(Long userId, Long deadlineId, String newDescription) {
        Deadline deadline = deadlineLoader.loadByIdAndUserId(deadlineId, userId);
        deadline.changeDescription(newDescription);
    }

    public void markAsCompleted(Long userId, Long deadlineId) {
        Deadline deadline = deadlineLoader.loadByIdAndUserId(deadlineId, userId);
        deadline.markAsCompleted();
    }
}