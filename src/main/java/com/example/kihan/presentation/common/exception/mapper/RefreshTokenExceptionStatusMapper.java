package com.example.kihan.presentation.common.exception.mapper;

import com.example.kihan.domain.auth.InvalidRefreshTokenException;
import com.example.kihan.domain.auth.RefreshTokenException;
import com.example.kihan.domain.auth.RefreshTokenNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenExceptionStatusMapper implements DomainExceptionStatusMapper {

    @Override
    public boolean supports(RuntimeException exception) {
        return exception instanceof RefreshTokenException;
    }

    @Override
    public HttpStatus map(RuntimeException exception) {
        if (exception instanceof RefreshTokenNotFoundException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if  (exception instanceof InvalidRefreshTokenException) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.BAD_REQUEST;
    }

}