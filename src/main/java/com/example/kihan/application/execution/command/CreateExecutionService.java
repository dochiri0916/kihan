package com.example.kihan.application.execution.command;

import com.example.kihan.domain.deadline.Deadline;
import com.example.kihan.domain.execution.Execution;
import com.example.kihan.infrastructure.persistence.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreateExecutionService {

    private final ExecutionRepository executionRepository;

    @Transactional
    public Long execute(final Deadline deadline, final LocalDate scheduledDate) {
        if (executionRepository.existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(
                deadline.getId(), scheduledDate)) {
            return null;
        }

        Execution execution = Execution.create(deadline, scheduledDate);
        return executionRepository.save(execution).getId();
    }

}