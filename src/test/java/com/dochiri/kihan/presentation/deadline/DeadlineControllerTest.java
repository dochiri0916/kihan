package com.dochiri.kihan.presentation.deadline;

import com.dochiri.kihan.application.deadline.command.DeleteDeadlineService;
import com.dochiri.kihan.application.deadline.command.RegisterDeadlineService;
import com.dochiri.kihan.application.deadline.command.UpdateDeadlineService;
import com.dochiri.kihan.application.deadline.dto.DeadlineDetail;
import com.dochiri.kihan.application.deadline.dto.RegisterDeadlineCommand;
import com.dochiri.kihan.application.deadline.dto.UpdateDeadlineCommand;
import com.dochiri.kihan.application.deadline.query.DeadlineQueryService;
import com.dochiri.kihan.domain.deadline.DeadlineNotFoundException;
import com.dochiri.kihan.domain.deadline.DeadlineType;
import com.dochiri.kihan.domain.deadline.RecurrencePattern;
import com.dochiri.kihan.domain.deadline.RecurrenceRule;
import com.dochiri.kihan.domain.user.UserRole;
import com.dochiri.kihan.infrastructure.security.jwt.JwtPrincipal;
import com.dochiri.kihan.presentation.common.exception.ExceptionStatusMapper;
import com.dochiri.kihan.presentation.common.exception.GlobalExceptionHandler;
import com.dochiri.kihan.presentation.common.exception.mapper.DeadlineExceptionStatusMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeadlineController 테스트")
class DeadlineControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RegisterDeadlineService registerDeadlineService;

    @Mock
    private UpdateDeadlineService updateDeadlineService;

    @Mock
    private DeleteDeadlineService deleteDeadlineService;

    @Mock
    private DeadlineQueryService deadlineQueryService;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ExceptionStatusMapper exceptionStatusMapper =
                new ExceptionStatusMapper(List.of(new DeadlineExceptionStatusMapper()));

        mockMvc = MockMvcBuilders
                .standaloneSetup(new DeadlineController(
                        registerDeadlineService,
                        updateDeadlineService,
                        deleteDeadlineService,
                        deadlineQueryService
                ))
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
    @DisplayName("ONE_TIME 등록 성공 시 201과 Location 헤더를 반환한다")
    void shouldRegisterOneTimeDeadlineAndReturnCreatedLocation() throws Exception {
        authenticate(1L);
        when(registerDeadlineService.execute(any())).thenReturn(101L);

        mockMvc.perform(post("/api/deadlines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "프로젝트 제출",
                                  "description": "최종 보고서 제출",
                                  "type": "ONE_TIME",
                                  "dueDate": "2026-03-01T09:00:00",
                                  "pattern": null,
                                  "interval": null,
                                  "startDate": null,
                                  "endDate": null
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/deadlines/101"));

        ArgumentCaptor<RegisterDeadlineCommand> captor = ArgumentCaptor.forClass(RegisterDeadlineCommand.class);
        verify(registerDeadlineService).execute(captor.capture());
        assertEquals(1L, captor.getValue().userId());
    }

    @Test
    @DisplayName("RECURRING 등록 시 recurrenceRule을 생성해 서비스에 전달한다")
    void shouldRegisterRecurringDeadlineWithRecurrenceRule() throws Exception {
        authenticate(1L);
        when(registerDeadlineService.execute(any())).thenReturn(102L);

        mockMvc.perform(post("/api/deadlines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "주간 회의",
                                  "description": "팀 회의",
                                  "type": "RECURRING",
                                  "dueDate": null,
                                  "pattern": "WEEKLY",
                                  "interval": 1,
                                  "startDate": "2026-02-01",
                                  "endDate": "2026-12-31"
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<RegisterDeadlineCommand> captor = ArgumentCaptor.forClass(RegisterDeadlineCommand.class);
        verify(registerDeadlineService).execute(captor.capture());
        RecurrenceRule rule = captor.getValue().recurrenceRule();
        assertEquals(1L, captor.getValue().userId());
        assertEquals(RecurrencePattern.WEEKLY, rule.getPattern());
        assertEquals(1, rule.getInterval());
        assertEquals(LocalDate.of(2026, 2, 1), rule.getStartDate());
        assertEquals(LocalDate.of(2026, 12, 31), rule.getEndDate());
    }

    @Test
    @DisplayName("RECURRING 등록에서 interval이 누락되면 기본값 1로 처리한다")
    void shouldUseDefaultIntervalWhenRecurringIntervalIsMissing() throws Exception {
        authenticate(1L);
        when(registerDeadlineService.execute(any())).thenReturn(103L);

        mockMvc.perform(post("/api/deadlines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "주간 회의",
                                  "description": "팀 회의",
                                  "type": "RECURRING",
                                  "dueDate": null,
                                  "pattern": "WEEKLY",
                                  "interval": null,
                                  "startDate": "2026-02-01",
                                  "endDate": null
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<RegisterDeadlineCommand> captor = ArgumentCaptor.forClass(RegisterDeadlineCommand.class);
        verify(registerDeadlineService).execute(captor.capture());
        RecurrenceRule rule = captor.getValue().recurrenceRule();
        assertEquals(1, rule.getInterval());
        assertEquals(LocalDate.of(2026, 2, 1), rule.getStartDate());
        assertNull(rule.getEndDate());
    }

    @Test
    @DisplayName("등록 요청이 유효하지 않으면 400을 반환한다")
    void shouldReturnBadRequestWhenRegisterRequestIsInvalid() throws Exception {
        authenticate(1L);

        mockMvc.perform(post("/api/deadlines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "설명",
                                  "type": null,
                                  "dueDate": null,
                                  "pattern": null,
                                  "interval": null,
                                  "startDate": null,
                                  "endDate": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("VALIDATION_FAILED"));

        verify(registerDeadlineService, never()).execute(any());
    }

    @Test
    @DisplayName("기한 단건 조회 성공 시 응답을 반환한다")
    void shouldGetDeadlineById() throws Exception {
        authenticate(1L);
        when(deadlineQueryService.getById(1L, 10L))
                .thenReturn(new DeadlineDetail(
                        10L,
                        "프로젝트 제출",
                        "설명",
                        DeadlineType.ONE_TIME,
                        LocalDateTime.of(2026, 3, 1, 9, 0),
                        null,
                        LocalDateTime.of(2026, 2, 1, 10, 0)
                ));

        mockMvc.perform(get("/api/deadlines/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("프로젝트 제출"))
                .andExpect(jsonPath("$.type").value("ONE_TIME"));
    }

    @Test
    @DisplayName("기한 목록 조회 성공 시 배열 응답을 반환한다")
    void shouldGetAllDeadlines() throws Exception {
        authenticate(1L);
        when(deadlineQueryService.getAllByUserId(1L)).thenReturn(List.of(
                new DeadlineDetail(10L, "운동", "설명", DeadlineType.ONE_TIME, LocalDateTime.of(2026, 2, 21, 9, 0), null, null),
                new DeadlineDetail(11L, "독서", "설명", DeadlineType.ONE_TIME, LocalDateTime.of(2026, 2, 22, 9, 0), null, null)
        ));

        mockMvc.perform(get("/api/deadlines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("운동"))
                .andExpect(jsonPath("$[1].title").value("독서"));
    }

    @Test
    @DisplayName("기한 수정 성공 시 204를 반환하고 업데이트 명령을 전달한다")
    void shouldUpdateDeadline() throws Exception {
        authenticate(1L);

        mockMvc.perform(patch("/api/deadlines/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "새 제목",
                                  "description": "새 설명"
                                }
                                """))
                .andExpect(status().isNoContent());

        ArgumentCaptor<UpdateDeadlineCommand> captor = ArgumentCaptor.forClass(UpdateDeadlineCommand.class);
        verify(updateDeadlineService).update(captor.capture());
        assertEquals(1L, captor.getValue().userId());
        assertEquals(10L, captor.getValue().deadlineId());
        assertEquals("새 제목", captor.getValue().title());
        assertEquals("새 설명", captor.getValue().description());
    }

    @Test
    @DisplayName("기한 삭제 성공 시 204를 반환한다")
    void shouldDeleteDeadline() throws Exception {
        authenticate(1L);

        mockMvc.perform(delete("/api/deadlines/10"))
                .andExpect(status().isNoContent());

        verify(deleteDeadlineService).execute(1L, 10L);
    }

    @Test
    @DisplayName("기한 조회 중 not found 예외가 발생하면 404를 반환한다")
    void shouldReturnNotFoundWhenDeadlineMissing() throws Exception {
        authenticate(1L);
        when(deadlineQueryService.getById(1L, 999L)).thenThrow(new DeadlineNotFoundException(999L));

        mockMvc.perform(get("/api/deadlines/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("기한 항목을 찾을 수 없습니다: 999"));
    }

    private void authenticate(Long userId) {
        JwtPrincipal principal = new JwtPrincipal(userId, UserRole.USER);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
