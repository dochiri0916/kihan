package com.example.kihan.application.deadline.command;

import com.example.kihan.application.deadline.query.DeadlineLoader;
import com.example.kihan.domain.deadline.Deadline;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteDeadlineService {

    private final DeadlineLoader deadlineLoader;

    @Transactional
    public void execute(final Long userId, final Long deadlineId) {
        Deadline deadline = deadlineLoader.loadByIdAndUserId(deadlineId, userId);
        deadline.delete();
    }

}