package com.example.kihan.presentation.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

import static java.util.Objects.*;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends BaseExceptionHandler {

    private final ExceptionStatusMapper exceptionStatusMapper;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problemDetail.setTitle("VALIDATION_FAILED");
        problemDetail.setDetail("입력값이 올바르지 않습니다.");
        problemDetail.setProperty("path", request.getRequestURI());
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        problemDetail.setProperty(
                "errors",
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> Map.of(
                                "field", error.getField(),
                                "message", requireNonNullElse(
                                        error.getDefaultMessage(),
                                        "유효하지 않은 값입니다."
                                )
                        ))
                        .toList()
        );

        return problemDetail;
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = exceptionStatusMapper.map(exception);
        return handle(status, exception, request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception, HttpServletRequest request) {
        return handle(HttpStatus.INTERNAL_SERVER_ERROR, exception, request);
    }

}