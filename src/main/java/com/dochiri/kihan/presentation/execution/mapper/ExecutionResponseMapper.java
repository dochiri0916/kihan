package com.dochiri.kihan.presentation.execution.mapper;

import com.dochiri.kihan.application.execution.dto.ExecutionDetail;
import com.dochiri.kihan.application.execution.dto.ExecutionStatusChangedResult;
import com.dochiri.kihan.presentation.execution.response.ExecutionQueryResponse;
import com.dochiri.kihan.presentation.execution.response.ExecutionStatusChangedResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExecutionResponseMapper {

    public ExecutionQueryResponse toExecutionQueryResponse(ExecutionDetail detail) {
        return ExecutionQueryResponse.from(detail);
    }

    public List<ExecutionQueryResponse> toExecutionQueryResponses(List<ExecutionDetail> details) {
        return details.stream()
                .map(this::toExecutionQueryResponse)
                .toList();
    }

    public ExecutionStatusChangedResponse toExecutionStatusChangedResponse(ExecutionStatusChangedResult result) {
        return ExecutionStatusChangedResponse.from(result);
    }

}