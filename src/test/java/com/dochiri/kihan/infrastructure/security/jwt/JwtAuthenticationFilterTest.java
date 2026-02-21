package com.dochiri.kihan.infrastructure.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 테스트")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Bearer 토큰이 있으면 인증을 설정하고 체인을 진행한다")
    void shouldSetAuthenticationAndContinueWhenBearerTokenExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        TrackingFilterChain chain = new TrackingFilterChain();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("principal", null, List.of());
        when(jwtAuthenticationConverter.convert("valid-token")).thenReturn(authentication);

        jwtAuthenticationFilter.doFilter(request, response, chain);

        assertEquals(1, chain.invocationCount);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtAuthenticationConverter).convert("valid-token");
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 없이 체인을 진행한다")
    void shouldContinueWithoutAuthenticationWhenAuthorizationHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        TrackingFilterChain chain = new TrackingFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, chain);

        assertEquals(1, chain.invocationCount);
        assertEquals(null, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("토큰 변환 중 예외가 발생하면 예외를 전파한다")
    void shouldPropagateExceptionWhenConversionFails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        TrackingFilterChain chain = new TrackingFilterChain();

        when(jwtAuthenticationConverter.convert("bad-token"))
                .thenThrow(new BadCredentialsException("bad token"));

        assertThrows(BadCredentialsException.class,
                () -> jwtAuthenticationFilter.doFilter(request, response, chain));
        assertEquals(0, chain.invocationCount);
    }

    private static class TrackingFilterChain implements FilterChain {
        int invocationCount;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            invocationCount++;
        }
    }
}
