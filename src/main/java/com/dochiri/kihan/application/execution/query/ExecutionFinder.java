package com.dochiri.kihan.application.execution.query;

import com.dochiri.kihan.domain.execution.Execution;

import java.util.Optional;

public interface ExecutionFinder {

    Optional<Execution> findActiveById(Long executionId);

}