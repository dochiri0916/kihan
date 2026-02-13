package com.example.kihan.application.deadline.command;

import com.example.kihan.domain.deadline.Deadline;
import com.example.kihan.domain.deadline.DeadlineNotFoundException;
import com.example.kihan.infrastructure.persistence.DeadlineRepository;
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
