# KIHAN 코드 리뷰

## 반영 현황 (2026-02-21)

### 완료

- `1.1` `UpdateDeadlineService.markAsCompleted()`에 `@Transactional` 추가
- `1.2` `ReissueTokenFacade`에 `Clock` 주입 및 `LocalDateTime.now(clock)` 적용
- `1.3` `DeadlineRegisterRequest.toRecurrenceRule()`에서 `interval == null` 방어 추가
- `2.1` `IssueRefreshTokenService` 신규/기존 토큰 분기 리팩토링 (`rotate()` 중복 제거)
- `2.2` `ExecutionGenerationService` 건별 예외 격리 (`try-catch`) 적용
- `2.3` `LoginResult`의 `User` 엔티티 직접 노출 제거 (`UserDetail` 사용)
- `2.4` `ExecutionQueryService.findByDeadlineId()`에서 `DeadlineQueryService.getById()`로 소유권 검증 위임
- `3.2` `DateRangeQuery`에 `startDate <= endDate` 검증 추가

### 보류

- `3.1` `markAsCompleted()` API 미노출/중복 정리
- `3.3` RecurrenceRule 수정 API
- `3.4` `DELAYED -> PENDING` 복원 API
- `3.5` 페이지네이션

### 검증

- `./gradlew test --tests "com.dochiri.kihan.application.auth.facade.ReissueTokenFacadeTest" --tests "com.dochiri.kihan.application.auth.command.IssueRefreshTokenServiceTest" --tests "com.dochiri.kihan.presentation.deadline.DeadlineControllerTest" --tests "com.dochiri.kihan.presentation.execution.ExecutionControllerTest"` 통과
- `./gradlew test` 전체 통과

## 1. 버그 / 잠재적 오류

### 1.1 `UpdateDeadlineService.markAsCompleted()` — `@Transactional` 누락

**파일**: `application/deadline/command/UpdateDeadlineService.java`

```java
@Transactional
public void update(UpdateDeadlineCommand command) { ... }  // 있음

public void markAsCompleted(Long userId, Long deadlineId) {  // ← 없음
    Deadline deadline = deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(deadlineId, userId);
    deadline.markAsCompleted(LocalDateTime.now(clock));
}
```

트랜잭션이 없으면 JPA dirty checking이 flush되지 않아 `deletedAt` 변경이 DB에 반영되지 않는다.

**수정**: `@Transactional` 추가.

---

### 1.2 `ReissueTokenFacade` — `Clock` 미사용

**파일**: `application/auth/facade/ReissueTokenFacade.java`

```java
refreshToken.verifyNotExpired(LocalDateTime.now());  // ← Clock 없이 직접 호출
```

`LoginFacade`는 `Clock`을 주입받아 `LocalDateTime.now(clock)`을 쓰는데 `ReissueTokenFacade`만 직접 호출. 테스트에서 시간 제어가 불가능하고, 시스템 시계 일관성도 깨진다.

**수정**: `Clock clock` 필드 주입 후 `LocalDateTime.now(clock)` 사용.

---

### 1.3 `DeadlineRegisterRequest.toRecurrenceRule()` — Integer → int 언박싱 NPE

**파일**: `presentation/deadline/request/DeadlineRegisterRequest.java`

```java
public RecurrenceRule toRecurrenceRule() {
    if (pattern == null) return null;
    return RecurrenceRule.create(pattern, interval, startDate, endDate);
    //                                     ↑ Integer → int 언박싱, interval이 null이면 NPE
}
```

RECURRING 타입에서 `interval`을 요청에 포함하지 않으면 도메인 검증 전에 NPE가 발생한다.

**수정**: 요청 필드에 `@NotNull` Bean Validation 추가하거나, `toRecurrenceRule()` 내부에서 null 체크 후 도메인 예외를 직접 던진다.

---

## 2. 설계 개선

### 2.1 `IssueRefreshTokenService` — 신규 토큰에 `rotate()` 중복 호출

**파일**: `application/auth/command/IssueRefreshTokenService.java`

```java
public void execute(IssueRefreshTokenCommand command) {
    RefreshToken refreshToken = refreshTokenRepository.findByUserId(command.userId())
            .orElseGet(() ->
                    RefreshToken.issue(command.token(), command.userId(), command.expiresAt())
                    // ↑ 이미 token, expiresAt 설정 완료
            );

    refreshToken.rotate(command.token(), command.expiresAt());  // ← 신규 생성 시 동일 값으로 중복 설정
    refreshTokenRepository.save(refreshToken);
}
```

기존 토큰이면 `rotate()`가 맞지만, 신규 생성 시엔 `issue()`로 이미 모든 필드가 설정됐으므로 `rotate()` 재호출이 중복. 의도가 코드에서 드러나지 않는다.

**수정**:

```java
public void execute(IssueRefreshTokenCommand command) {
    RefreshToken refreshToken = refreshTokenRepository.findByUserId(command.userId())
            .map(existing -> {
                existing.rotate(command.token(), command.expiresAt());
                return existing;
            })
            .orElseGet(() -> RefreshToken.issue(command.token(), command.userId(), command.expiresAt()));

    refreshTokenRepository.save(refreshToken);
}
```

---

### 2.2 `ExecutionGenerationService` — 한 Deadline 실패 시 전체 롤백

**파일**: `application/execution/scheduler/ExecutionGenerationService.java`

```java
@Transactional
public void generateExecutionsForToday() {
    List<Deadline> deadlines = deadlineQueryService.findAllActive();
    for (Deadline deadline : deadlines) {
        if (shouldCreateExecution(deadline, today)) {
            createExecutionService.execute(deadline, today);  // 하나 실패 → 전체 롤백
        }
    }
}
```

1000개 Deadline 중 하나에서 예외가 나면 앞서 생성된 실행 전부 롤백. 스케줄러는 건별로 격리돼야 한다.

**수정**: `try-catch`로 건별 격리.

```java
for (Deadline deadline : deadlines) {
    try {
        if (shouldCreateExecution(deadline, today)) {
            createExecutionService.execute(deadline, today);
        }
    } catch (Exception e) {
        log.error("Failed to create execution for deadline {}: {}", deadline.getId(), e.getMessage());
    }
}
```

---

### 2.3 `LoginResult` — 도메인 엔티티 `User` 직접 노출

**파일**: `application/auth/dto/LoginResult.java`

```java
public record LoginResult(
        User user,         // ← 비밀번호, deletedAt 등 민감 필드 포함된 엔티티
        String accessToken,
        String refreshToken
) { }
```

Application DTO가 도메인 엔티티를 그대로 담아 Presentation까지 전달. `user.getPassword()` 등 민감 정보가 의도치 않게 접근 가능한 상태로 흐른다.

**수정**: `UserDetail`로 변환해서 전달.

```java
public record LoginResult(
        UserDetail user,
        String accessToken,
        String refreshToken
) { }
```

---

### 2.4 `ExecutionQueryService` — `DeadlineRepository` 직접 의존 + 반환값 미사용

**파일**: `application/execution/query/ExecutionQueryService.java`

```java
public List<ExecutionDetail> findByDeadlineId(Long userId, Long deadlineId) {
    deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(deadlineId, userId);  // 결과 버림
    return executionRepository.findByDeadlineIdAndDeletedAtIsNull(deadlineId)...
}
```

소유권 확인만 하고 반환값을 버린다. `DeadlineQueryService.getById()`가 이미 소유권 체크 + 존재 확인을 포함하므로 위임하면 의도가 명확해진다.

**수정**:

```java
public List<ExecutionDetail> findByDeadlineId(Long userId, Long deadlineId) {
    deadlineQueryService.getById(userId, deadlineId);  // 소유권 + 존재 확인
    return executionRepository.findByDeadlineIdAndDeletedAtIsNull(deadlineId)...
}
```

---

## 3. 미구현 / 누락 기능

### 3.1 `markAsCompleted()` — API 미노출

`UpdateDeadlineService.markAsCompleted()`가 구현되어 있지만 컨트롤러에 엔드포인트가 없다.

더 큰 문제는 `DeleteDeadlineService.execute()`와 내부 동작이 동일하다는 점이다.

| | `DeleteDeadlineService.execute()` | `markAsCompleted()` |
|-|---|---|
| 내부 호출 | `deadline.delete(now)` | `deadline.markAsCompleted(now)` → `this.delete(now)` |
| API | `DELETE /api/deadlines/{id}` | 없음 |

의미가 다르다면 API를 노출하고, 같다면 `markAsCompleted()`를 제거해야 한다.

---

### 3.2 DateRangeQuery 검증 없음

**파일**: `presentation/execution/ExecutionController.java`

```java
@GetMapping
public ResponseEntity<List<ExecutionResponse>> getExecutionsByDateRange(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate   // startDate > endDate를 막는 코드 없음
) { ... }
```

`startDate`가 `endDate`보다 늦으면 빈 결과를 조용히 반환. `DateRangeQuery` 생성자나 컨트롤러에서 검증이 필요하다.

---

### 3.3 RecurrenceRule 수정 불가

`Deadline.update()`는 title, description만 변경 가능. 반복 패턴(MONTHLY → WEEKLY), interval, startDate, endDate 변경이 불가능하다.

반복 일정 관리가 핵심 도메인인 만큼 반복 규칙 자체를 변경하는 기능이 없다는 건 도메인 공백이다.

---

### 3.4 DELAYED → PENDING 복원 없음

현재 상태 전이:

```
PENDING → DONE    ✓
PENDING → DELAYED ✓
DELAYED → DONE    ✓
DELAYED → PENDING ✗
DONE    → PENDING ✗ (의도적)
```

실수로 지연 처리한 경우 되돌릴 수 없다. `markAsPending()` 메서드 추가를 고려할 수 있다.

---

### 3.5 페이지네이션 없음

`getAllByUserId`, `findByDateRange`, `findByDeadlineId` 전부 전체 조회. Execution이 연간 365개씩 쌓이면 수만 건을 한 번에 반환하는 상황이 된다.

---

## 4. 총 요약

| 구분 | 항목 | 심각도 |
|------|------|--------|
| 버그 | `markAsCompleted()` `@Transactional` 누락 | 높음 |
| 버그 | `ReissueTokenFacade` Clock 미사용 | 중간 |
| 버그 | `toRecurrenceRule()` Integer 언박싱 NPE | 중간 |
| 설계 | `IssueRefreshTokenService` rotate 중복 호출 | 낮음 |
| 설계 | 스케줄러 트랜잭션 격리 없음 | 중간 |
| 설계 | `LoginResult`에 User 엔티티 직접 노출 | 낮음 |
| 기능 | `markAsCompleted()` vs `delete()` 중복 + API 미노출 | 중간 |
| 기능 | RecurrenceRule 수정 API 없음 | 높음 |
| 기능 | DateRangeQuery 검증 없음 | 중간 |
| 기능 | DELAYED → PENDING 복원 없음 | 중간 |
| 기능 | 페이지네이션 없음 | 낮음 |
