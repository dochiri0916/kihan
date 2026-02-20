package com.example.kihan.presentation.common.exception.mapper;

import com.example.kihan.domain.user.DuplicateEmailException;
import com.example.kihan.domain.user.UserException;
import com.example.kihan.domain.user.UserAccessDeniedException;
import com.example.kihan.domain.user.InactiveUserException;
import com.example.kihan.domain.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class UserExceptionStatusMapper implements DomainExceptionStatusMapper {

    @Override
    public boolean supports(RuntimeException exception) {
        return exception instanceof UserException;
    }

    @Override
    public HttpStatus map(RuntimeException exception) {
        if (exception instanceof UserNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (exception instanceof DuplicateEmailException) {
            return HttpStatus.CONFLICT;
        }
        if (exception instanceof InactiveUserException) {
            return HttpStatus.FORBIDDEN;
        }
        if (exception instanceof UserAccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.BAD_REQUEST;
    }

}
