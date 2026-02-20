# KIHAN 프로젝트 전체 분석

## 1. 프로젝트 개요

반복 일정과 기한을 관리하고, 매일 자동으로 실행(Execution)을 생성하여 완료/지연 상태를 추적하는 시스템.

- **Tech Stack**: Java 25, Spring Boot 4.0.2, Spring Security + JWT, Spring Data JPA, H2 (MySQL mode), Lombok, Hibernate Envers, OpenAPI 3.0
- **아키텍처**: 실용적 레이어드 + DDD/Hexagonal 선택적 적용

---

## 2. 패키지 구조

```
com.example.kihan/
├── domain/                          # 순수 도메인 (프레임워크 무관)
│   ├── BaseEntity.java              # 공통 엔티티 (audit, soft delete)
│   ├── auth/                        # RefreshToken 엔티티, 예외
│   ├── deadline/                    # Deadline(Aggregate Root), RecurrenceRule(VO), 예외
│   ├── execution/                   # Execution 엔티티, 예외
│   └── user/                        # User 엔티티, UserRole, 예외
├── application/                     # 유스케이스 (CQRS-lite)
│   ├── auth/command/                # 로그인, 토큰 발급/폐기
│   ├── auth/facade/                 # LoginFacade, ReissueTokenFacade
│   ├── deadline/command/            # 등록, 수정, 삭제
│   ├── deadline/query/              # 조회, Finder/Loader 인터페이스
│   ├── execution/command/           # 생성, 완료, 지연 처리
│   ├── execution/query/             # 조회 (기한별, 기간별)
│   ├── execution/scheduler/         # ExecutionGenerationService
│   └── user/command|query/          # 회원가입, 사용자 조회
├── presentation/                    # API 계층
│   ├── auth|deadline|execution|user # 컨트롤러, Request/Response DTO
│   └── common/exception/           # GlobalExceptionHandler, Mapper Chain
└── infrastructure/                  # 기술 구현
    ├── config/                      # Security, JPA, Scheduling, Swagger
    ├── security/                    # JWT, Cookie, Audit
    ├── persistence/                 # Repository 구현체
    └── scheduler/                   # Daily/Cleanup 스케줄러
```

---

## 3. 도메인 모델

### Deadline (Aggregate Root)

| 필드 | 타입 | 설명 |
|------|------|------|
| userId | Long | 소유자 |
| title | String | 제목 (필수) |
| description | String | 설명 (TEXT) |
| type | DeadlineType | ONE_TIME / RECURRING |
| dueDate | LocalDateTime | 단발성 기한일 |
| recurrenceRule | RecurrenceRule | 반복 규칙 (Embedded VO) |

**검증 규칙**: ONE_TIME은 dueDate 필수 + recurrenceRule 불가, RECURRING은 그 반대.

### RecurrenceRule (Value Object)

| 필드 | 타입 | 설명 |
|------|------|------|
| pattern | RecurrencePattern | DAILY / WEEKLY / MONTHLY / YEARLY |
| interval | int | 반복 간격 (>= 1) |
| startDate | LocalDate | 시작일 |
| endDate | LocalDate | 종료일 (선택) |

### Execution (Entity)

| 필드 | 타입 | 설명 |
|------|------|------|
| deadline | Deadline | 소속 기한 (ManyToOne, Lazy) |
| scheduledDate | LocalDate | 예정일 |
| status | ExecutionStatus | PENDING / DONE / DELAYED |
| completedAt | LocalDateTime | 완료 시각 |

### User / RefreshToken

- **User**: email(unique), password(BCrypt), name, role(USER/ADMIN), lastLoginAt
- **RefreshToken**: token(unique), userId(unique), expiresAt + rotate/verify 행위

---

## 4. API 엔드포인트

### Public

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/users/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 → JWT + HttpOnly 쿠키 |
| POST | `/api/auth/reissue` | 토큰 재발급 |

### Protected (JWT 필요)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/users/me` | 내 정보 |
| GET | `/api/users/{id}` | 사용자 조회 |
| POST | `/api/deadlines` | 기한 등록 (201 + Location) |
| GET | `/api/deadlines` | 내 기한 목록 |
| GET | `/api/deadlines/{id}` | 기한 상세 |
| PATCH | `/api/deadlines/{id}` | 기한 수정 |
| DELETE | `/api/deadlines/{id}` | 기한 삭제 (soft) |
| GET | `/api/executions/{id}` | 실행 상세 |
| GET | `/api/executions/deadline/{id}` | 기한별 실행 목록 |
| GET | `/api/executions?startDate&endDate` | 기간별 실행 목록 |
| PATCH | `/api/executions/{id}/done` | 완료 처리 |
| PATCH | `/api/executions/{id}/delayed` | 지연 처리 |
| POST | `/api/auth/logout` | 로그아웃 |

---

## 5. 잘한 점

### 5.1 도메인 설계가 탄탄함

- **팩토리 메서드 패턴**: 모든 엔티티가 `register()`, `create()`, `issue()` 등 의미 있는 팩토리 메서드 사용. 생성자 직접 노출 없이 도메인 불변식을 보장함.
- **검증 로직이 도메인 안에 있음**: `Deadline.validate()`, `RecurrenceRule.validate()` 등 비즈니스 규칙이 도메인 객체 내부에 캡슐화.
- **Setter 없음**: 상태 변경은 `markAsDone()`, `markAsCompleted()`, `rotate()` 등 의미 있는 행위 메서드로만 가능.
- **Value Object 활용**: `RecurrenceRule`을 `@Embeddable` VO로 구현하여 불변성 보장.

### 5.2 예외 설계가 깔끔함

- **도메인 예외 계층 구조**: 각 도메인별 추상 부모 예외(`DeadlineException`, `UserException` 등) 아래 구체 예외 배치.
- **정적 팩토리 메서드**: `InvalidDeadlineRuleException.oneTimeDueDateRequired()` 같은 네이밍으로 예외 생성 의도가 명확.
- **ExceptionStatusMapper Chain**: 새 도메인 예외 추가 시 Mapper만 구현하면 됨. OCP 준수.
- **RFC 7807 ProblemDetail**: 표준 에러 응답 포맷 사용.

### 5.3 아키텍처 일관성

- **CQRS-lite**: command/query 패키지 분리가 일관됨. 읽기 서비스에 `@Transactional(readOnly = true)` 적용.
- **Facade 패턴**: `LoginFacade`가 인증 → 토큰 생성 → 리프레시 토큰 발급을 깔끔하게 조합. 트랜잭션 경계도 여기서 관리.
- **Finder/Loader 인터페이스**: `DeadlineFinder`(Optional 반환) / `DeadlineLoader`(예외 throw) 구분으로 호출측 의도 명확화.
- **ArchUnit 테스트**: 레이어 의존성, 네이밍 컨벤션, API 패턴 등을 코드로 강제.

### 5.4 보안 설계

- **Access Token + HttpOnly Refresh Token**: XSS 공격으로부터 리프레시 토큰 보호.
- **서버사이드 리프레시 토큰 저장**: 토큰 탈취 시 서버에서 무효화 가능.
- **Token Rotation**: 재발급 시 리프레시 토큰도 갱신하여 토큰 재사용 공격 방지.
- **소유권 검증**: `verifyOwnership()`으로 다른 사용자의 리소스 접근 차단.

### 5.5 기타

- **Soft Delete + Audit**: `BaseEntity`에 `deletedAt`, `createdBy`, `updatedBy` 통합.
- **Hibernate Envers**: Deadline에 변경 이력 추적.
- **@ConfigurationProperties record**: 타입 안전한 설정 관리.
- **Swagger 예제**: `DeadlineController`에 ONE_TIME/RECURRING 예제 제공.

---

## 6. 리팩토링 제안

### 6.1 [높음] `ExecutionQueryService.findByDateRange()` — DB 필터링 누락

```java
// 현재: 전체 Execution을 메모리에 올린 후 Java에서 날짜 필터링
return executionRepository.findByDeadlineIdInAndDeletedAtIsNull(userDeadlineIds).stream()
        .filter(execution -> !execution.getScheduledDate().isBefore(query.startDate())
                && !execution.getScheduledDate().isAfter(query.endDate()))
        .map(ExecutionDetail::from)
        .toList();
```

**문제**: 데이터 증가 시 성능 저하. 전체 Execution을 메모리에 로드 후 필터링.

**개선**: Repository에 `findByDeadlineIdInAndScheduledDateBetweenAndDeletedAtIsNull()` 쿼리 추가하여 DB 레벨에서 필터링.

### 6.2 [높음] `ExecutionQueryService.findByDeadlineId()` — 비효율적 소유권 검증

```java
// 현재: 사용자의 모든 Deadline ID를 조회한 후 contains 체크
List<Long> userDeadlineIds = deadlineRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
        .map(deadline -> deadline.getId())
        .toList();
if (!userDeadlineIds.contains(deadlineId)) { return List.of(); }
```

**문제**: 단일 Deadline의 실행을 조회하는데 사용자의 전체 Deadline을 로드.

**개선**: `deadlineRepository.findByIdAndUserIdAndDeletedAtIsNull(deadlineId, userId)` 단건 조회로 소유권 확인.

### 6.3 [높음] `JwtAuthenticationFilter` — 잘못된 토큰도 조용히 통과

```java
} catch (Exception e) {
    SecurityContextHolder.clearContext();
}
filterChain.doFilter(request, response);  // 만료/변조 토큰도 계속 진행
```

**문제**: 만료되거나 변조된 토큰이 제공되면 인증 없이 요청이 계속됨. 인증이 필요한 엔드포인트는 Security에서 거부하지만, 에러 메시지가 "토큰 만료"가 아닌 "인증 필요"로 나와서 클라이언트가 원인을 파악하기 어려움.

**개선**: 만료 토큰(`ExpiredJwtException`)과 변조 토큰(`SignatureException`)을 구분하여 적절한 에러 응답 반환. 또는 최소한 로그 기록.

### 6.4 [중간] `ExceptionStatusMapper` — 매핑 실패 시 기본값이 400

```java
public HttpStatus map(RuntimeException exception) {
    for (DomainExceptionStatusMapper mapper : mappers) {
        if (mapper.supports(exception)) { return mapper.map(exception); }
    }
    return HttpStatus.BAD_REQUEST;  // NullPointerException도 400이 됨
}
```

**문제**: 매핑되지 않는 `RuntimeException`(예: `NullPointerException`, `IllegalStateException`)이 400 Bad Request로 반환됨. 서버 오류가 클라이언트 오류로 위장.

**개선**: 기본값을 `HttpStatus.INTERNAL_SERVER_ERROR`로 변경하거나, `GlobalExceptionHandler.handleRuntimeException()`에서 도메인 예외 여부를 먼저 판별.

### 6.5 [중간] `BaseEntity.delete()` — `LocalDateTime.now()` 직접 호출

```java
public void delete() {
    this.deletedAt = LocalDateTime.now();
}
```

**문제**: 테스트에서 시간 제어 불가. `Execution.markAsDone()`에도 동일 이슈(`this.completedAt = LocalDateTime.now()`).

**개선**: `Clock`을 주입받거나, 파라미터로 시간을 전달받는 방식으로 변경.

### 6.6 [중간] `Deadline`과 `Execution`의 관계 — Aggregate 경계 불명확

`Execution`이 `@ManyToOne`으로 `Deadline`을 직접 참조하면서, CLAUDE.md에는 "Execution = Entity within Deadline aggregate"라고 정의. 하지만 실제로는:
- `ExecutionRepository`가 독립적으로 존재
- `Execution`이 `Deadline` 없이 독립 조회/수정됨
- `Deadline`이 `Execution`의 생명주기를 관리하지 않음

**현실**: Execution은 사실상 별도 Aggregate. 문서와 구현을 일치시키는 게 좋음.

### 6.7 [중간] `CreateExecutionService.execute()` — null 반환

```java
public Long execute(final Deadline deadline, final LocalDate scheduledDate) {
    if (executionRepository.existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull(...)) {
        return null;  // 이미 존재하면 null
    }
    ...
}
```

**문제**: null 반환은 호출측에서 의도를 파악하기 어려움.

**개선**: `Optional<Long>` 반환, 또는 이미 존재하는 경우 기존 ID 반환.

### 6.8 [중간] `CookieProvider` — 수동 Set-Cookie 헤더 조립

```java
StringBuilder cookie = new StringBuilder();
cookie.append(name).append('=').append(value).append("; ");
// ...
response.addHeader("Set-Cookie", cookie.toString());
```

**문제**: 쿠키 값에 특수문자가 포함되면 인젝션 위험. Spring의 `ResponseCookie` API를 사용하면 자동으로 안전하게 처리됨.

**개선**: `ResponseCookie.from(name, value).httpOnly(true)...build()` + `response.addHeader("Set-Cookie", cookie.toString())` 사용.

### 6.9 [낮음] `GET /api/users/{id}` — 권한 검증 없음

현재 인증된 사용자라면 누구나 다른 사용자의 정보를 ID로 조회할 수 있음. 관리자 전용이거나 본인만 가능하도록 제한 필요.

### 6.10 [낮음] `ExecutionGenerationService` — application 계층에서 infrastructure 직접 참조

```java
private final DeadlineRepository deadlineRepository;  // infrastructure
```

`DeadlineQueryService`(Finder/Loader)를 통해 접근하는 게 레이어 규칙에 더 부합. 현재 ArchUnit 테스트에서 이 규칙을 검사하고 있다면 위반 가능성 있음.

### 6.11 [낮음] 반복 패턴 날짜 계산 — 엣지 케이스

`shouldCreateMonthlyExecution()`에서 `date.getDayOfMonth() == rule.getStartDate().getDayOfMonth()` 비교:
- 시작일이 31일이면 30일짜리 달에는 실행이 생성되지 않음
- 시작일이 29~31일이면 2월에 누락됨

이 동작이 의도적이라면 문서화 필요. 아니라면 "해당 월의 마지막 날" 폴백 로직 추가.

### 6.12 [낮음] 테스트 커버리지 부족

현재 테스트는 ArchUnit 아키텍처 테스트 + 컨텍스트 로딩 테스트만 존재. 도메인 로직, 서비스 로직, 컨트롤러 통합 테스트가 없음.

**우선순위 추천**:
1. 도메인 단위 테스트 (Deadline 검증, Execution 상태 전이, RecurrenceRule 검증)
2. `ExecutionGenerationService` — 반복 패턴 계산 로직 테스트
3. 인증/인가 통합 테스트

---

## 7. 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────┐
│                  Presentation                    │
│  Controller → Request DTO → Response DTO         │
│  GlobalExceptionHandler ← ExceptionStatusMapper  │
└──────────────────────┬──────────────────────────┘
                       │ depends on
┌──────────────────────▼──────────────────────────┐
│                  Application                     │
│  ┌─────────┐  ┌────────┐  ┌──────────────────┐  │
│  │ Command  │  │ Query  │  │     Facade       │  │
│  │ Services │  │Services│  │ (orchestration)  │  │
│  └────┬─────┘  └───┬────┘  └───────┬──────────┘  │
│       │            │               │              │
│       └────────────┼───────────────┘              │
└──────────────────────┬──────────────────────────┘
                       │ depends on
┌──────────────────────▼──────────────────────────┐
│                    Domain                        │
│  Deadline ◄── RecurrenceRule (VO)                │
│      ▲                                           │
│      │ ManyToOne                                 │
│  Execution        User        RefreshToken       │
│                                                  │
│  도메인 예외 (프레임워크 무관)                       │
└──────────────────────────────────────────────────┘
                       ▲ implements
┌──────────────────────┴──────────────────────────┐
│                Infrastructure                    │
│  JPA Repositories, JWT, Cookie, Scheduler        │
│  Security Config, Properties                     │
└──────────────────────────────────────────────────┘
```

---

## 8. 보안 흐름

```
[로그인]
Client → POST /api/auth/login (email, password)
  → UserAuthenticationService (BCrypt 검증)
  → JwtTokenGenerator (Access + Refresh 토큰 생성)
  → IssueRefreshTokenService (DB 저장)
  → Response: { accessToken } + Set-Cookie: refreshToken (HttpOnly)

[인증된 요청]
Client → GET /api/deadlines (Authorization: Bearer <accessToken>)
  → JwtAuthenticationFilter (토큰 파싱 → SecurityContext 설정)
  → Controller (@AuthenticationPrincipal JwtPrincipal)
  → Service (verifyOwnership)

[토큰 재발급]
Client → POST /api/auth/reissue (Cookie: refreshToken)
  → RefreshTokenVerifier (JWT 파싱 + category 확인)
  → DB에서 RefreshToken 로드 → 만료/소유권 검증
  → Token Rotation (새 토큰으로 교체)
  → Response: { newAccessToken } + Set-Cookie: newRefreshToken
```

---

## 9. 총평

**잘 설계된 학습 프로젝트**. DDD 개념(Aggregate Root, VO, 팩토리 메서드), CQRS-lite, Facade 패턴을 실용적으로 적용했고 과도한 추상화 없이 깔끔하게 유지. 예외 체계와 보안 설계가 특히 성숙함.

**핵심 개선 포인트**:
1. **쿼리 성능**: 메모리 필터링 → DB 쿼리로 전환 (6.1, 6.2)
2. **에러 처리 정확성**: 매핑 실패 기본값 수정 + JWT 에러 구분 (6.3, 6.4)
3. **테스트**: 도메인/서비스 단위 테스트 추가 (6.12)
4. **시간 제어**: `LocalDateTime.now()` 의존 제거 (6.5)
