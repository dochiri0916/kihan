package com.example.pragmatic.presentation.common.exception.mapper;

import org.springframework.http.HttpStatus;

public interface DomainExceptionStatusMapper {

    boolean supports(RuntimeException exception);

    HttpStatus map(RuntimeException exception);

}