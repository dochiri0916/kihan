package com.dochiri.kihan.presentation.common.exception;

import com.dochiri.kihan.presentation.common.exception.mapper.DomainExceptionStatusMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExceptionStatusMapper {

    private final List<DomainExceptionStatusMapper> mappers;

    public HttpStatus map(RuntimeException exception) {
        for (DomainExceptionStatusMapper mapper : mappers) {
            if (mapper.supports(exception)) {
                return mapper.map(exception);
            }
        }
        log.warn(
                "No exception mapper matched. Falling back to 500. exceptionType={}, message={}",
                exception.getClass().getName(),
                exception.getMessage()
        );
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
