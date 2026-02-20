package com.dochiri.kihan.presentation.common.exception.mapper;

import com.dochiri.kihan.domain.deadline.DeadlineAccessDeniedException;
import com.dochiri.kihan.domain.deadline.DeadlineException;
import com.dochiri.kihan.domain.deadline.DeadlineNotFoundException;
import com.dochiri.kihan.domain.deadline.InvalidDeadlineRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class DeadlineExceptionStatusMapper implements DomainExceptionStatusMapper {

    @Override
    public boolean supports(RuntimeException exception) {
        return exception instanceof DeadlineException;
    }

    @Override
    public HttpStatus map(RuntimeException exception) {
        if (exception instanceof DeadlineNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (exception instanceof DeadlineAccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }
        if (exception instanceof InvalidDeadlineRuleException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.BAD_REQUEST;
    }

}