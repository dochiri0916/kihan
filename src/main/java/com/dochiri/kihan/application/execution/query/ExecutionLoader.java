package com.dochiri.kihan.application.execution.query;

import com.dochiri.kihan.domain.execution.Execution;

public interface ExecutionLoader {

    Execution loadActiveById(Long executionId);

}