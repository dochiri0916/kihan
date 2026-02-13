package com.example.kihan.presentation.common.exception;

import com.example.kihan.presentation.common.exception.mapper.DomainExceptionStatusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExceptionStatusMapper {

    private final List<DomainExceptionStatusMapper> mappers;

    public HttpStatus map(RuntimeException exception) {
        for (DomainExceptionStatusMapper mapper : mappers) {
            if (mapper.supports(exception)) {
                return mapper.map(exception);
            }
        }
        return HttpStatus.BAD_REQUEST;
    }

}