package com.dochiri.kihan.application.execution.scheduler;

import com.dochiri.kihan.domain.execution.Execution;
import com.dochiri.kihan.domain.execution.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OverdueExecutionCompletionService {

    private final ExecutionRepository executionRepository;
    private final Clock clock;

    @Transactional
    public int completeOverdueOneTimeExecutions() {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime now = LocalDateTime.now(clock);
        List<Execution> executions = executionRepository.findOverdueOneTimeAndNotDone(today);

        for (Execution execution : executions) {
            execution.markAsDone(now);
        }

        return executions.size();
    }
}
