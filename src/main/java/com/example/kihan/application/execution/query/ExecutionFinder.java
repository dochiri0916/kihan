package com.example.kihan.application.execution.query;

import com.example.kihan.domain.execution.Execution;

import java.util.Optional;

public interface ExecutionFinder {

    Optional<Execution> findActiveById(Long executionId);

}