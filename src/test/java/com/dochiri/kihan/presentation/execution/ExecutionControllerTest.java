package com.dochiri.kihan.presentation.execution;

import com.dochiri.kihan.application.execution.command.MarkExecutionAsPausedService;
import com.dochiri.kihan.application.execution.command.MarkExecutionAsDoneService;
import com.dochiri.kihan.application.execution.command.MarkExecutionAsInProgressService;
import com.dochiri.kihan.application.execution.command.MarkExecutionByDeadlineAsDoneService;
import com.dochiri.kihan.application.execution.dto.ExecutionDetail;
import com.dochiri.kihan.application.execution.dto.ExecutionStatusChangedResult;
import com.dochiri.kihan.application.execution.query.ExecutionQueryService;
import com.dochiri.kihan.application.execution.usecase.GetExecutionsByDateRangeUseCase;
import com.dochiri.kihan.domain.execution.exception.ExecutionAlreadyCompletedException;
import com.dochiri.kihan.domain.execution.exception.InvalidExecutionDateRangeException;
import com.dochiri.kihan.domain.execution.ExecutionStatus;
import com.dochiri.kihan.domain.user.UserRole;
import com.dochiri.kihan.infrastructure.security.jwt.JwtPrincipal;
import com.dochiri.kihan.presentation.common.exception.ExceptionStatusMapper;
import com.dochiri.kihan.presentation.common.exception.GlobalExceptionHandler;
import com.dochiri.kihan.presentation.common.exception.mapper.ExecutionExceptionStatusMapper;
import com.dochiri.kihan.presentation.execution.mapper.ExecutionResponseMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExecutionController 테스트")
class ExecutionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MarkExecutionAsDoneService markExecutionAsDoneService;

    @Mock
    private MarkExecutionByDeadlineAsDoneService markExecutionByDeadlineAsDoneService;

    @Mock
    private MarkExecutionAsPausedService markExecutionAsPausedService;

    @Mock
    private MarkExecutionAsInProgressService markExecutionAsInProgressService;

    @Mock
    private ExecutionQueryService executionQueryService;

    @Mock
    private GetExecutionsByDateRangeUseCase getExecutionsByDateRangeUseCase;

    @BeforeEach
    void setUp() {
        ExceptionStatusMapper exceptionStatusMapper =
                new ExceptionStatusMapper(List.of(new ExecutionExceptionStatusMapper()));

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExecutionController(
                        markExecutionAsDoneService,
                        markExecutionByDeadlineAsDoneService,
                        markExecutionAsPausedService,
                        markExecutionAsInProgressService,
                        executionQueryService,
                        getExecutionsByDateRangeUseCase,
                        new ExecutionResponseMapper()
                ))
                .setControllerAdvice(new GlobalExceptionHandler(exceptionStatusMapper, Clock.systemUTC()))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("실행 단건 조회 성공 시 응답을 반환한다")
    void shouldGetExecutionById() throws Exception {
        authenticate(1L);
        when(executionQueryService.getById(1L, 10L))
                .thenReturn(new ExecutionDetail(
                        10L,
                        3L,
                        LocalDate.of(2026, 2, 21),
                        ExecutionStatus.IN_PROGRESS,
                        null
                ));

        mockMvc.perform(get("/api/executions/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.deadlineId").value(3))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("기한별 실행 목록 조회 성공 시 배열 응답을 반환한다")
    void shouldGetExecutionsByDeadline() throws Exception {
        authenticate(1L);
        when(executionQueryService.getByDeadlineId(1L, 3L)).thenReturn(List.of(
                new ExecutionDetail(10L, 3L, LocalDate.of(2026, 2, 21), ExecutionStatus.IN_PROGRESS, null),
                new ExecutionDetail(11L, 3L, LocalDate.of(2026, 2, 22), ExecutionStatus.DONE, LocalDateTime.of(2026, 2, 22, 11, 0))
        ));

        mockMvc.perform(get("/api/executions/deadline/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[1].status").value("DONE"));
    }

    @Test
    @DisplayName("기간별 실행 목록 조회 시 use case를 통해 응답을 반환한다")
    void shouldGetExecutionsByDateRange() throws Exception {
        authenticate(1L);
        when(getExecutionsByDateRangeUseCase.execute(
                1L,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        )).thenReturn(List.of(
                new ExecutionDetail(10L, 3L, LocalDate.of(2026, 2, 21), ExecutionStatus.IN_PROGRESS, null)
        ));

        mockMvc.perform(get("/api/executions")
                        .param("startDate", "2026-02-01")
                        .param("endDate", "2026-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].scheduledDate").value("2026-02-21"));

        verify(getExecutionsByDateRangeUseCase).execute(
                1L,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );
    }

    @Test
    @DisplayName("실행 완료 처리 성공 시 200과 상태 변경 응답을 반환한다")
    void shouldMarkExecutionAsDone() throws Exception {
        authenticate(1L);
        when(markExecutionAsDoneService.execute(1L, 10L))
                .thenReturn(new ExecutionStatusChangedResult(
                        10L,
                        3L,
                        ExecutionStatus.DONE,
                        LocalDate.of(2026, 2, 21),
                        LocalDateTime.of(2026, 2, 21, 10, 30)
                ));

        mockMvc.perform(patch("/api/executions/10/done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("DONE"));

        verify(markExecutionAsDoneService).execute(1L, 10L);
    }

    @Test
    @DisplayName("실행 중지 처리 성공 시 200과 상태 변경 응답을 반환한다")
    void shouldMarkExecutionAsPaused() throws Exception {
        authenticate(1L);
        when(markExecutionAsPausedService.execute(1L, 10L))
                .thenReturn(new ExecutionStatusChangedResult(
                        10L,
                        3L,
                        ExecutionStatus.PAUSED,
                        LocalDate.of(2026, 2, 21),
                        null
                ));

        mockMvc.perform(patch("/api/executions/10/paused"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PAUSED"));

        verify(markExecutionAsPausedService).execute(1L, 10L);
    }

    @Test
    @DisplayName("기한 기준 완료 처리 성공 시 200과 상태 변경 응답을 반환한다")
    void shouldMarkExecutionAsDoneByDeadline() throws Exception {
        authenticate(1L);
        when(markExecutionByDeadlineAsDoneService.execute(1L, 3L))
                .thenReturn(new ExecutionStatusChangedResult(
                        10L,
                        3L,
                        ExecutionStatus.DONE,
                        LocalDate.of(2026, 2, 21),
                        LocalDateTime.of(2026, 2, 21, 10, 30)
                ));

        mockMvc.perform(patch("/api/executions/deadline/3/done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("DONE"));

        verify(markExecutionByDeadlineAsDoneService).execute(1L, 3L);
    }

    @Test
    @DisplayName("실행 재개 처리 성공 시 200과 상태 변경 응답을 반환한다")
    void shouldMarkExecutionAsInProgress() throws Exception {
        authenticate(1L);
        when(markExecutionAsInProgressService.execute(1L, 10L))
                .thenReturn(new ExecutionStatusChangedResult(
                        10L,
                        3L,
                        ExecutionStatus.IN_PROGRESS,
                        LocalDate.of(2026, 2, 21),
                        null
                ));

        mockMvc.perform(patch("/api/executions/10/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(markExecutionAsInProgressService).execute(1L, 10L);
    }

    @Test
    @DisplayName("기간 파라미터 형식이 잘못되면 예외 핸들러에 의해 500을 반환한다")
    void shouldReturnInternalServerErrorWhenDateFormatIsInvalid() throws Exception {
        authenticate(1L);

        mockMvc.perform(get("/api/executions")
                        .param("startDate", "bad-date")
                        .param("endDate", "2026-02-28"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.exception").value("MethodArgumentTypeMismatchException"));
    }

    @Test
    @DisplayName("startDate가 endDate보다 늦으면 400을 반환한다")
    void shouldReturnBadRequestWhenStartDateIsAfterEndDate() throws Exception {
        authenticate(1L);
        doThrow(InvalidExecutionDateRangeException.startDateAfterEndDate(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 2, 28)
        )).when(getExecutionsByDateRangeUseCase).execute(
                1L,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 2, 28)
        );

        mockMvc.perform(get("/api/executions")
                        .param("startDate", "2026-03-01")
                        .param("endDate", "2026-02-28"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exception").value("InvalidExecutionDateRangeException"));
    }

    @Test
    @DisplayName("실행 완료 중 충돌 예외가 발생하면 409를 반환한다")
    void shouldReturnConflictWhenExecutionAlreadyCompleted() throws Exception {
        authenticate(1L);
        doThrow(ExecutionAlreadyCompletedException.withDate(LocalDate.of(2026, 2, 21)))
                .when(markExecutionAsDoneService)
                .execute(1L, 10L);

        mockMvc.perform(patch("/api/executions/10/done"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("이미 완료된 실행입니다: 2026-02-21"));
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
