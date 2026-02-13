package com.example.kihan.presentation.common.exception.mapper;

import org.springframework.http.HttpStatus;

public interface DomainExceptionStatusMapper {

    boolean supports(RuntimeException exception);

    HttpStatus map(RuntimeException exception);

}