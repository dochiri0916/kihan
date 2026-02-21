package com.dochiri.kihan.presentation.common.exception.mapper;

import com.dochiri.kihan.domain.execution.ExecutionAlreadyCompletedException;
import com.dochiri.kihan.domain.execution.ExecutionException;
import com.dochiri.kihan.domain.execution.InvalidExecutionStatusTransitionException;
import com.dochiri.kihan.domain.execution.ExecutionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ExecutionExceptionStatusMapper implements DomainExceptionStatusMapper {

    @Override
    public boolean supports(final RuntimeException exception) {
        return exception instanceof ExecutionException;
    }

    @Override
    public HttpStatus map(final RuntimeException exception) {
        if (exception instanceof ExecutionNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (exception instanceof ExecutionAlreadyCompletedException) {
            return HttpStatus.CONFLICT;
        }
        if (exception instanceof InvalidExecutionStatusTransitionException) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.BAD_REQUEST;
    }

}
