package com.example.kihan.presentation.common.exception.mapper;

import com.example.kihan.domain.user.DuplicateEmailException;
import com.example.kihan.domain.user.UserException;
import com.example.kihan.domain.user.UserNotActiveException;
import com.example.kihan.domain.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserExceptionStatusMapper implements DomainExceptionStatusMapper {

    private static final Map<Class<? extends UserException>, HttpStatus> STATUS_MAP =
            Map.of(
                    UserNotFoundException.class, HttpStatus.NOT_FOUND,
                    DuplicateEmailException.class, HttpStatus.CONFLICT,
                    UserNotActiveException.class, HttpStatus.FORBIDDEN
            );

    @Override
    public boolean supports(RuntimeException exception) {
        return exception instanceof UserException;
    }

    @Override
    public HttpStatus map(RuntimeException exception) {
        return STATUS_MAP.getOrDefault(((UserException) exception).getClass(), HttpStatus.BAD_REQUEST);
    }

}