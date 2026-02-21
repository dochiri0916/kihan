package com.dochiri.kihan.presentation.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
public class BaseExceptionHandler {

    private final Clock clock;

    protected BaseExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    protected ProblemDetail handle(HttpStatus status, Exception exception, HttpServletRequest request) {
        logException(status, exception, request);
        return createProblemDetail(status, exception, request);
    }

    protected LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private void logException(HttpStatus status, Exception exception, HttpServletRequest request) {
        boolean serverError = status.is5xxServerError();
        if (serverError) {
            log.error("[{}]: uri={}, method={}, message={}",
                    exception.getClass().getSimpleName(),
                    request.getRequestURI(),
                    request.getMethod(),
                    exception.getMessage(),
                    exception
            );
            return;
        }

        log.warn("[{}] uri={}, method={}, message={}",
                exception.getClass().getSimpleName(),
                request.getRequestURI(),
                request.getMethod(),
                exception.getMessage(),
                exception
        );
    }

    private ProblemDetail createProblemDetail(HttpStatus status, Exception exception, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());

        problemDetail.setProperty("timestamp", now());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());
        problemDetail.setProperty("path", request.getRequestURI());

        return problemDetail;
    }

}
