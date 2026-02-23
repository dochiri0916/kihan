package com.dochiri.kihan.application.execution.command;

import com.dochiri.kihan.domain.deadline.Deadline;
import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateExecutionService {

    private final ExecutionRepository executionRepository;

    @Transactional
    public Optional<Long> execute(Deadline deadline, LocalDate scheduledDate) {
        if (executionRepository.existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(
                deadline.getId(), scheduledDate)) {
            return Optional.empty();
        }

        Execution execution = Execution.create(deadline, scheduledDate);
        return Optional.of(executionRepository.save(execution).getId());
    }

}