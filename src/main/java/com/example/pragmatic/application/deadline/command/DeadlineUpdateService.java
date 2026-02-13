package com.example.pragmatic.application.deadline.command;

import com.example.pragmatic.domain.deadline.Deadline;
import com.example.pragmatic.domain.deadline.DeadlineNotFoundException;
import com.example.pragmatic.infrastructure.persistence.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeadlineUpdateService {

    private final DeadlineRepository deadlineRepository;

    public void changeTitle(Long deadlineId, String newTitle) {
        Deadline deadline = findDeadline(deadlineId);
        deadline.changeTitle(newTitle);
    }

    public void changeDescription(Long deadlineId, String newDescription) {
        Deadline deadline = findDeadline(deadlineId);
        deadline.changeDescription(newDescription);
    }

    public void markAsCompleted(Long deadlineId) {
        Deadline deadline = findDeadline(deadlineId);
        deadline.markAsCompleted();
    }

    private Deadline findDeadline(Long deadlineId) {
        return deadlineRepository.findByIdAndDeletedAtIsNull(deadlineId)
                .orElseThrow(() -> new DeadlineNotFoundException(deadlineId));
    }
}
