package com.example.kihan.presentation.common.exception.mapper;

import com.example.kihan.domain.auth.AuthenticationException;
import com.example.kihan.domain.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationExceptionStatusMapper implements DomainExceptionStatusMapper {

    @Override
    public boolean supports(RuntimeException exception) {
        return exception instanceof AuthenticationException;
    }

    @Override
    public HttpStatus map(RuntimeException exception) {
        if (exception instanceof InvalidCredentialsException) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.BAD_REQUEST;
    }

}