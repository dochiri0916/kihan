# KIHAN 프로젝트 전체 분석

## 1. 프로젝트 개요

반복 일정과 기한을 관리하고, 매일 자동으로 실행(Execution)을 생성하여 완료/지연 상태를 추적하는 시스템.

- **Tech Stack**: Java 25, Spring Boot 4.0.2, Spring Security + JWT, Spring Data JPA, H2 (MySQL mode), Lombok, Hibernate Envers, OpenAPI 3.0
- **아키텍처**: 실용적 레이어드 + DDD/Hexagonal 선택적 적용
- **패키지**: `com.dochiri.kihan`

---

## 2. 패키지 구조

```
com.dochiri.kihan/
├── domain/                          # 순수 도메인 (프레임워크 무관)
│   ├── BaseEntity.java              # 공통 엔티티 (audit, soft delete)
│   ├── auth/                        # RefreshToken 엔티티, 예외
│   ├── deadline/                    # Deadline(Aggregate Root), RecurrenceRule(VO), 예외
│   │                                  DeadlineRepository(도메인 인터페이스)
│   ├── execution/                   # Execution 엔티티(독립 Aggregate), 예외
│   │                                  ExecutionRepository(도메인 인터페이스)
│   └── user/                        # User 엔티티, UserRole, 예외
│                                      UserRepository(도메인 인터페이스)
├── application/                     # 유스케이스 (CQRS-lite)
│   ├── auth/command/                # 로그인, 토큰 발급/폐기
│   ├── auth/facade/                 # LoginFacade, ReissueTokenFacade
│   ├── deadline/command/            # 등록, 수정, 삭제
│   ├── deadline/query/              # 조회
│   ├── execution/command/           # 생성, 완료, 지연 처리
│   ├── execution/query/             # 조회 (기한별, 기간별)
│   ├── execution/scheduler/         # ExecutionGenerationService
│   └── user/command|query/          # 회원가입, 사용자 조회
├── presentation/                    # API 계층
│   ├── auth|deadline|execution|user # 컨트롤러, Request/Response DTO
│   └── common/exception/           # GlobalExceptionHandler, Mapper Chain
└── infrastructure/                  # 기술 구현
    ├── config/                      # Security, JPA, Scheduling, Swagger, Time(Clock)
    │   └── properties/              # JwtProperties, CorsProperties, SecurityProperties
    ├── security/                    # JWT, Audit
    │   └── jwt/                     # JwtProvider, JwtAuthenticationConverter,
    │                                  JwtAuthenticationFilter, JwtTokenGenerator,
    │                                  RefreshTokenVerifier, JwtPrincipal, JwtTokenResult
    ├── persistence/                 # Repository 어댑터 (JpaXxxRepository → XxxJpaRepository)
    └── scheduler/                   # Daily/Cleanup 스케줄러
```

---

## 3. 도메인 모델

### BaseEntity (MappedSuperclass)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 자동 생성 PK |
| createdAt | LocalDateTime | @CreatedDate |
| updatedAt | LocalDateTime | @LastModifiedDate |
| createdBy | String | @CreatedBy (audit) |
| updatedBy | String | @LastModifiedBy (audit) |
| deletedAt | LocalDateTime | soft delete 마커 |

**메서드**: `delete(LocalDateTime)`, `restore()`, `isDeleted()`

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

**주요 메서드**: `register(...)` 팩토리, `update(title, description)`, `markAsCompleted(LocalDateTime)`, `verifyOwnership(Long userId)`

### RecurrenceRule (Value Object)

| 필드 | 타입 | 설명 |
|------|------|------|
| pattern | RecurrencePattern | DAILY / WEEKLY / MONTHLY / YEARLY |
| interval | int | 반복 간격 (>= 1) |
| startDate | LocalDate | 시작일 |
| endDate | LocalDate | 종료일 (선택) |

### Execution (독립 Aggregate)

| 필드 | 타입 | 설명 |
|------|------|------|
| deadline | Deadline | 연관 기한 (ManyToOne, Lazy) |
| scheduledDate | LocalDate | 예정일 |
| status | ExecutionStatus | PENDING / DONE / DELAYED |
| completedAt | LocalDateTime | 완료 시각 |

**상태 전이**: PENDING→DONE (`markAsDone`), PENDING→DELAYED (`markAsDelayed`), DELAYED→DONE 허용. DONE→DONE, DONE→DELAYED는 `ExecutionAlreadyCompletedException` 발생.

### User

| 필드 | 타입 | 설명 |
|------|------|------|
| email | String | unique, non-null |
| password | String | BCrypt 인코딩 |
| name | String | non-null |
| role | UserRole | USER / ADMIN (기본 USER) |
| lastLoginAt | LocalDateTime | nullable |

**주요 메서드**: `register(email, password, name)` 팩토리, `updateLastLoginAt(LocalDateTime)`, `verifyAccessBy(Long requestUserId, UserRole requestUserRole)` — 본인 또는 ADMIN 아니면 `UserAccessDeniedException`

### RefreshToken

| 필드 | 타입 | 설명 |
|------|------|------|
| token | String | unique, non-null |
| userId | Long | unique (user당 1개) |
| expiresAt | LocalDateTime | 만료 시각 |

**주요 메서드**: `issue(token, userId, expiresAt)` 팩토리, `verifyNotExpired(LocalDateTime)`, `verifyOwnership(Long userId)`, `rotate(newToken, newExpiresAt)`

---

## 4. API 엔드포인트

### Public

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/users/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 → `{ userId, role, accessToken, refreshToken }` 응답 body |
| POST | `/api/auth/reissue` | 토큰 재발급 (refreshToken을 request body로 전달) |

### Protected (JWT 필요)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/users/me` | 내 정보 |
| GET | `/api/users/{id}` | 사용자 조회 (본인/ADMIN만) |
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
| POST | `/api/auth/logout` | 로그아웃 (refreshToken body로 전달 시 서버 토큰 폐기) |

---

## 5. 잘한 점

### 5.1 도메인 설계가 탄탄함

- **팩토리 메서드 패턴**: 모든 엔티티가 `register()`, `create()`, `issue()` 등 의미 있는 팩토리 메서드 사용. 생성자 직접 노출 없이 도메인 불변식을 보장.
- **검증 로직이 도메인 안에 있음**: `Deadline.validate()`, `RecurrenceRule.validate()` 등 비즈니스 규칙이 도메인 객체 내부에 캡슐화.
- **Setter 없음**: 상태 변경은 `markAsDone()`, `markAsCompleted()`, `rotate()` 등 의미 있는 행위 메서드로만 가능.
- **Value Object 활용**: `RecurrenceRule`을 `@Embeddable` VO로 구현하여 불변성 보장.
- **도메인 Repository 인터페이스**: `DeadlineRepository`, `ExecutionRepository`, `UserRepository`가 도메인 계층에 인터페이스로 존재하고 infrastructure에서 구현. 의존 역전 원칙 준수.

### 5.2 예외 설계가 깔끔함

- **도메인 예외 계층 구조**: 각 도메인별 추상 부모 예외(`DeadlineException`, `UserException` 등) 아래 구체 예외 배치.
- **정적 팩토리 메서드**: `InvalidDeadlineRuleException.oneTimeDueDateRequired()` 같은 네이밍으로 예외 생성 의도가 명확.
- **ExceptionStatusMapper Chain**: 새 도메인 예외 추가 시 Mapper만 구현하면 됨. OCP 준수.
- **RFC 7807 ProblemDetail**: 표준 에러 응답 포맷 사용.
- **매핑 실패 시 500 반환**: `ExceptionStatusMapper`가 매핑되지 않는 예외에 대해 `INTERNAL_SERVER_ERROR`를 반환하여 서버 오류를 클라이언트 오류로 위장하지 않음.

### 5.3 아키텍처 일관성

- **CQRS-lite**: command/query 패키지 분리가 일관됨. 읽기 서비스에 `@Transactional(readOnly = true)` 적용.
- **Facade 패턴**: `LoginFacade`가 인증 → 토큰 생성 → 리프레시 토큰 발급을 깔끔하게 조합. 트랜잭션 경계도 여기서 관리.
- **ArchUnit 테스트**: 레이어 의존성, 네이밍 컨벤션, API 패턴 등을 코드로 강제.
- **Repository 어댑터 패턴**: 도메인 인터페이스 → `JpaXxxRepository`(어댑터) → `XxxJpaRepository`(Spring Data) 3단 구조로 의존 역전.

### 5.4 보안 설계

- **서버사이드 리프레시 토큰 저장**: 토큰 탈취 시 서버에서 무효화 가능.
- **Token Rotation**: 재발급 시 리프레시 토큰도 갱신하여 토큰 재사용 공격 방지.
- **소유권 검증**: `verifyOwnership()`으로 다른 사용자의 리소스 접근 차단.
- **JwtAuthenticationConverter**: 토큰 파싱 → 사용자 존재 확인 → Authentication 생성을 단일 책임으로 분리. Filter에서 분리하여 테스트 용이성 확보.
- **인가 검증을 도메인으로 이동**: `User.verifyAccessBy(requestUserId, requestUserRole)` — Controller가 아닌 도메인에서 인가 규칙 캡슐화.
- **JWT 실패 원인 구분**: `ExpiredJwtException` → `CredentialsExpiredException`, 기타 `JwtException` → `BadCredentialsException`으로 변환하여 `JwtAuthenticationEntryPoint`에서 만료/유효하지 않음 구분 응답.

### 5.5 기타

- **Soft Delete + Audit**: `BaseEntity`에 `deletedAt`, `createdBy`, `updatedBy` 통합.
- **Hibernate Envers**: Deadline에 변경 이력 추적 (`@Audited` 유지, `DeadlineRevisionRepository`는 YAGNI 원칙으로 삭제).
- **@ConfigurationProperties record**: 타입 안전한 설정 관리 (`JwtProperties`, `CorsProperties`, `SecurityProperties`).
- **Swagger 예제**: `DeadlineController`에 ONE_TIME/RECURRING 예제 제공.
- **TimeConfig + Clock**: `Clock` 빈 제공으로 테스트에서 시간 제어 가능.
- **ExecutionGenerationService 멱등성**: `existsByDeadlineIdAndScheduledDateAndDeletedAtIsNull`으로 중복 생성 방지.

---

## 6. 보안 흐름

### 모바일/API 클라이언트용 토큰 방식 (바디 토큰)

```
[로그인]
Client → POST /api/auth/login { email, password }
  → UserAuthenticationService (BCrypt 검증)
  → JwtTokenGenerator (Access + Refresh 토큰 생성)
  → IssueRefreshTokenService (DB 저장)
  → Response body: { userId, role, accessToken, refreshToken }

[인증된 요청]
Client → GET /api/deadlines (Authorization: Bearer <accessToken>)
  → JwtAuthenticationFilter (토큰 추출)
  → JwtAuthenticationConverter (파싱 → 사용자 존재 확인 → Authentication 생성)
  → SecurityContext 설정
  → Controller (@AuthenticationPrincipal JwtPrincipal)
  → Service (verifyOwnership)

[토큰 재발급]
Client → POST /api/auth/reissue { refreshToken }
  → RefreshTokenVerifier (JWT 파싱 + category 확인)
  → DB에서 RefreshToken 로드 → 만료/소유권 검증
  → Token Rotation (새 토큰으로 교체)
  → Response body: { userId, role, accessToken, refreshToken }

[로그아웃]
Client → POST /api/auth/logout { refreshToken } (또는 body 없이)
  → refreshToken 있으면 RevokeTokenService.revokeByToken()
  → Response: 204 No Content
```

---

## 7. 테스트 현황

### 도메인 단위 테스트

| 테스트 클래스 | 대상 | 테스트 수 |
|---------------|------|-----------|
| `BaseEntityTest` | delete, restore, isDeleted | 4개 |
| `DeadlineDomainTest` | 팩토리, 검증, 수정, 완료, 소유권 | 21개 |
| `RecurrenceRuleTest` | 생성, interval, 날짜 범위 | 8개 |
| `UserTest` | 등록, 필드 검증, audit, 인가 | 14개 |
| `RefreshTokenTest` | 발급, 만료, 소유권, rotation | 9개 |
| `ExecutionTest` | 생성, 상태 전이, 멱등성 | 13개 |

### 애플리케이션 서비스 테스트

| 테스트 클래스 | 대상 |
|---------------|------|
| `UserAuthenticationServiceTest` | 비밀번호 검증 성공/실패 |
| `IssueRefreshTokenServiceTest` | 토큰 생성/로테이션 |
| `RevokeTokenServiceTest` | 만료 토큰 삭제, 토큰별 폐기 |
| `LoginFacadeTest` | 로그인 전체 플로우 |
| `ReissueTokenFacadeTest` | 재발급 전체 플로우 |
| `RegisterDeadlineServiceTest` | 기한 등록 |
| `UpdateDeadlineServiceTest` | 기한 수정, 완료 처리 |
| `DeleteDeadlineServiceTest` | 기한 소프트 삭제 |
| `DeadlineQueryServiceTest` | 기한 조회, 소유권 |
| `CreateExecutionServiceTest` | 중복 방지 실행 생성 |
| `MarkExecutionAsDoneServiceTest` | 완료 처리 |
| `MarkExecutionAsDelayedServiceTest` | 지연 처리 |
| `RegisterUserServiceTest` | 회원가입, 중복 이메일 |

### 인프라 테스트

| 테스트 클래스 | 대상 |
|---------------|------|
| `JwtProviderTest` | 토큰 생성/파싱/검증/클레임 |
| `JwtTokenGeneratorTest` | 액세스+리프레시 토큰 생성 |
| `JwtAuthenticationConverterTest` | JWT → Authentication 변환, 예외 처리 |
| `JwtAuthenticationFilterTest` | Bearer 추출, SecurityContext 설정 |
| `RefreshTokenVerifierTest` | 리프레시 토큰 검증 |
| `AuditorAwareImplTest` | 감사 정보 (SecurityContext 기반) |
| `DailyExecutionSchedulerTest` | 스케줄러 호출 |
| `RefreshTokenCleanupSchedulerTest` | 만료 토큰 정리 스케줄러 |

### 컨트롤러 통합 테스트 (MockMvc)

| 테스트 클래스 | 대상 |
|---------------|------|
| `AuthControllerTest` | 로그인, 재발급, 로그아웃 (바디 토큰 방식) |
| `UserControllerTest` | 회원가입, 내 정보, 사용자 조회 |
| `DeadlineControllerTest` | CRUD, 검증 |
| `ExecutionControllerTest` | 조회, 상태 전이 |

### 아키텍처 테스트 (ArchUnit)

| 테스트 클래스 | 검증 대상 |
|---------------|-----------|
| `LayerDependencyTest` | 레이어 간 의존 방향 |
| `DomainModelTest` | 엔티티 단수명, 테이블 복수명 |
| `DependencyRuleTest` | 순환 의존 금지 |
| `CodingConventionTest` | API URI 패턴 `/api/{domain-plural}/` |
| `NamingConventionTest` | Controller/Service/Facade 네이밍 |
| `ApiConventionTest` | RestController + RequestMapping |

---

## 8. 아키텍처 다이어그램

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
│  Repository 인터페이스 + 도메인 예외               │
└──────────────────────────────────────────────────┘
                       ▲ implements
┌──────────────────────┴──────────────────────────┐
│                Infrastructure                    │
│  JPA Repository 어댑터, JWT, Scheduler           │
│  Security Config, Properties, Clock              │
└──────────────────────────────────────────────────┘
```

---

## 9. 총평

**성숙도 높은 학습 프로젝트**. DDD 개념(Aggregate Root, VO, 팩토리 메서드), CQRS-lite, Facade 패턴, Repository 어댑터 패턴을 실용적으로 적용. 과도한 추상화 없이 깔끔하게 유지하면서도 아키텍처 원칙을 ArchUnit으로 강제하는 점이 인상적.

테스트 커버리지가 대폭 확장되었음: 도메인 단위 → 애플리케이션 서비스 → 인프라(JWT, 스케줄러) → 컨트롤러(MockMvc)까지 전 계층 테스트 완비.

**현재 핵심 개선 포인트**:
1. **`ExecutionGenerationService` 반복 계산 로직 단위 테스트**: DAILY/WEEKLY/MONTHLY/YEARLY 각 패턴, interval, 윤년(2/29), 월말 엣지 케이스 검증
2. **바디 토큰 보안 고려**: 클라이언트 측 토큰 저장 전략 문서화 (localStorage vs secure storage)
