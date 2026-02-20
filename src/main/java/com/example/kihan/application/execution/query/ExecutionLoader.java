package com.example.kihan.application.execution.query;

import com.example.kihan.domain.execution.Execution;

public interface ExecutionLoader {

    Execution loadActiveById(Long executionId);

}