package com.example.kihan.infrastructure.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.net.URI;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        ProblemDetail problemDetail =
                ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        problemDetail.setTitle("UNAUTHORIZED");
        problemDetail.setDetail("인증이 필요합니다.");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("path", request.getRequestURI());
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        writeResponse(response, problemDetail);
    }

    private void writeResponse(
            HttpServletResponse response,
            ProblemDetail body
    ) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getWriter(), body);
    }

}