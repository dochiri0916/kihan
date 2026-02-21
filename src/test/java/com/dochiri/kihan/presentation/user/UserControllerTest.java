package com.dochiri.kihan.presentation.user;

import com.dochiri.kihan.application.user.command.RegisterUserService;
import com.dochiri.kihan.application.user.dto.UserDetail;
import com.dochiri.kihan.application.user.query.UserQueryService;
import com.dochiri.kihan.domain.user.DuplicateEmailException;
import com.dochiri.kihan.domain.user.UserAccessDeniedException;
import com.dochiri.kihan.domain.user.UserRole;
import com.dochiri.kihan.infrastructure.security.jwt.JwtPrincipal;
import com.dochiri.kihan.presentation.common.exception.ExceptionStatusMapper;
import com.dochiri.kihan.presentation.common.exception.GlobalExceptionHandler;
import com.dochiri.kihan.presentation.common.exception.mapper.UserExceptionStatusMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 테스트")
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RegisterUserService registerUserService;

    @Mock
    private UserQueryService userQueryService;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ExceptionStatusMapper exceptionStatusMapper =
                new ExceptionStatusMapper(List.of(new UserExceptionStatusMapper()));

        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(registerUserService, userQueryService))
                .setControllerAdvice(new GlobalExceptionHandler(exceptionStatusMapper))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("회원가입 성공 시 사용자 정보를 반환한다")
    void shouldRegisterAndReturnUserResponse() throws Exception {
        when(registerUserService.execute(any()))
                .thenReturn(new UserDetail(1L, "user@example.com", "홍길동", "USER"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123",
                                  "name": "홍길동"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("회원가입 요청이 유효하지 않으면 400을 반환한다")
    void shouldReturnBadRequestWhenRegisterRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "bad-email",
                                  "password": "short",
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("VALIDATION_FAILED"));

        verify(registerUserService, never()).execute(any());
    }

    @Test
    @DisplayName("회원가입 중 중복 이메일 예외가 발생하면 409를 반환한다")
    void shouldReturnConflictWhenEmailIsDuplicated() throws Exception {
        when(registerUserService.execute(any())).thenThrow(new DuplicateEmailException("dup@example.com"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "dup@example.com",
                                  "password": "password123",
                                  "name": "중복"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("이미 사용중인 이메일입니다: dup@example.com"));
    }

    @Test
    @DisplayName("내 정보 조회 시 principal userId로 조회한다")
    void shouldGetMeByPrincipalUserId() throws Exception {
        authenticate(10L, UserRole.USER);
        when(userQueryService.getActiveUser(10L))
                .thenReturn(new UserDetail(10L, "me@example.com", "나", "USER"));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("me@example.com"));

        verify(userQueryService).getActiveUser(10L);
    }

    @Test
    @DisplayName("사용자 조회 시 접근자 정보와 대상 ID를 함께 전달한다")
    void shouldGetUserWithAccessContext() throws Exception {
        authenticate(10L, UserRole.ADMIN);
        when(userQueryService.getActiveUserWithAccess(10L, UserRole.ADMIN, 20L))
                .thenReturn(new UserDetail(20L, "target@example.com", "대상", "USER"));

        mockMvc.perform(get("/api/users/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.name").value("대상"));

        verify(userQueryService).getActiveUserWithAccess(10L, UserRole.ADMIN, 20L);
    }

    @Test
    @DisplayName("사용자 조회 중 권한 예외가 발생하면 403을 반환한다")
    void shouldReturnForbiddenWhenAccessDenied() throws Exception {
        authenticate(10L, UserRole.USER);
        when(userQueryService.getActiveUserWithAccess(10L, UserRole.USER, 20L))
                .thenThrow(UserAccessDeniedException.forUser(20L));

        mockMvc.perform(get("/api/users/20"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("사용자 정보 조회 권한이 없습니다. userId=20"));
    }

    private void authenticate(Long userId, UserRole role) {
        JwtPrincipal principal = new JwtPrincipal(userId, role);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
