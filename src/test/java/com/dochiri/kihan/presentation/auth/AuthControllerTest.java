package com.dochiri.kihan.presentation.auth;

import com.dochiri.kihan.application.auth.command.RevokeTokenService;
import com.dochiri.kihan.application.auth.dto.LoginResult;
import com.dochiri.kihan.application.auth.facade.LoginFacade;
import com.dochiri.kihan.application.auth.facade.ReissueTokenFacade;
import com.dochiri.kihan.domain.auth.InvalidCredentialsException;
import com.dochiri.kihan.domain.user.User;
import com.dochiri.kihan.presentation.common.exception.ExceptionStatusMapper;
import com.dochiri.kihan.presentation.common.exception.GlobalExceptionHandler;
import com.dochiri.kihan.presentation.common.exception.mapper.AuthenticationExceptionStatusMapper;
import com.dochiri.kihan.presentation.common.exception.mapper.RefreshTokenExceptionStatusMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import jakarta.servlet.http.Cookie;

import java.lang.reflect.Field;
import java.time.Clock;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LoginFacade loginFacade;

    @Mock
    private ReissueTokenFacade reissueTokenFacade;

    @Mock
    private RevokeTokenService revokeTokenService;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ExceptionStatusMapper exceptionStatusMapper =
                new ExceptionStatusMapper(List.of(
                        new AuthenticationExceptionStatusMapper(),
                        new RefreshTokenExceptionStatusMapper()
                ));

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(
                        loginFacade,
                        reissueTokenFacade,
                        revokeTokenService
                ))
                .setControllerAdvice(new GlobalExceptionHandler(exceptionStatusMapper, Clock.systemUTC()))
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("로그인 성공 시 accessToken을 응답 본문으로 반환하고 refreshToken을 쿠키로 설정한다")
    void shouldLoginAndReturnAccessTokenAndSetRefreshTokenCookie() throws Exception {
        User user = userWithId(1L, "user@example.com", "홍길동");
        when(loginFacade.login(any())).thenReturn(LoginResult.from(user, "access-token", "refresh-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refreshToken=refresh-token")));
    }

    @Test
    @DisplayName("로그인 요청이 유효하지 않으면 400을 반환한다")
    void shouldReturnBadRequestWhenLoginRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-email",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("로그인 중 인증 예외가 발생하면 401을 반환한다")
    void shouldReturnUnauthorizedWhenLoginFails() throws Exception {
        when(loginFacade.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("재발급 성공 시 쿠키 refreshToken으로 accessToken을 반환하고 refreshToken 쿠키를 갱신한다")
    void shouldReissueWithRefreshTokenCookie() throws Exception {
        User user = userWithId(2L, "reissue@example.com", "철수");
        when(reissueTokenFacade.reissue("refresh-1"))
                .thenReturn(LoginResult.from(user, "new-access", "new-refresh"));

        mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie("refreshToken", "refresh-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refreshToken=new-refresh")));
    }

    @Test
    @DisplayName("로그아웃 시 쿠키 refreshToken이 있으면 토큰을 폐기한다")
    void shouldRevokeTokenWhenLogoutWithRefreshTokenCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refreshToken", "refresh-1")))
                .andExpect(status().isNoContent());

        verify(revokeTokenService).revokeByToken("refresh-1");
    }

    @Test
    @DisplayName("로그아웃 시 refreshToken 쿠키를 삭제한다")
    void shouldClearRefreshTokenCookieWhenLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refreshToken", "refresh-1")))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refreshToken=;")));
    }

    @Test
    @DisplayName("로그아웃 시 쿠키가 없으면 토큰을 폐기하지 않는다")
    void shouldNotRevokeTokenWhenLogoutWithoutCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent());

        verify(revokeTokenService, never()).revokeByToken(any());
    }

    private User userWithId(Long id, String email, String name) {
        User user = User.register(email, "encoded-password", name);
        try {
            Field idField = user.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
        return user;
    }
}
