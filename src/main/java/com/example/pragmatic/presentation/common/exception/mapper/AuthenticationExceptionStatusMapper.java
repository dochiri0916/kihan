package com.example.pragmatic.presentation.common.exception.mapper;

import com.example.pragmatic.domain.auth.AuthenticationException;
import com.example.pragmatic.domain.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthenticationExceptionStatusMapper implements DomainExceptionStatusMapper {

    private static final Map<Class<? extends AuthenticationException>, HttpStatus> STATUS_MAP =
            Map.of(
                    InvalidCredentialsException.class, HttpStatus.UNAUTHORIZED
            );

    @Override
    public boolean supports(RuntimeException exception) {
        return exception instanceof AuthenticationException;
    }

    @Override
    public HttpStatus map(RuntimeException exception) {
        return STATUS_MAP.getOrDefault(((AuthenticationException) exception).getClass(), HttpStatus.BAD_REQUEST);
    }

}