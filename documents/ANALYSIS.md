# KIHAN 프로젝트 전체 분석 (업데이트: 2026-02-21)

## 1. 프로젝트 개요

Kihan은 사용자의 기한(Deadline)과 실행(Execution)을 관리하는 백엔드 API다.
핵심 목표는 다음과 같다.

- 단발/반복 기한 등록 및 관리
- 기한 기반 실행 자동 생성
- 실행 상태 전이(진행/중지/완료)
- JWT 기반 인증/인가 및 리프레시 토큰 재발급

기술 스택

- Java 25
- Spring Boot 4.0.2
- Spring Security + JWT
- Spring Data JPA
- H2 (MySQL mode)
- springdoc-openapi

---

## 2. 아키텍처 구조

패키지 구조는 `domain -> application -> presentation`, 그리고 기술 구현을 위한 `infrastructure`로 나뉜다.

- `domain`: 엔티티, 값 객체, 도메인 예외, 도메인 Repository 인터페이스
- `application`: 유스케이스 서비스(command/query/facade)
- `presentation`: REST API 컨트롤러 + 요청/응답 DTO + 예외 응답 처리
- `infrastructure`: JPA 구현체, JWT 컴포넌트, 설정, 스케줄러

실질적으로는 레이어드 아키텍처 + 도메인 중심 설계를 혼합한 형태다.

---

## 3. 현재 도메인 모델 핵심

### 3.1 Deadline

`Deadline`은 기한 집합의 루트 엔티티다.

필드(핵심)

- `userId: Long`
- `title: String`
- `type: DeadlineType (ONE_TIME | RECURRING)`
- `dueDate: LocalDate` (ONE_TIME 전용)
- `recurrenceRule: RecurrenceRule` (RECURRING 전용)

현재 정책

- `description` 필드는 제거됨
- `dueDate`는 시간 없는 날짜(`LocalDate`) 기준
- ONE_TIME: `dueDate` 필수, `recurrenceRule` 금지
- RECURRING: `recurrenceRule` 필수, `dueDate` 금지

### 3.2 RecurrenceRule

값 객체(`@Embeddable`)이며 필드는 다음과 같다.

- `pattern: DAILY | WEEKLY | MONTHLY | YEARLY`
- `startDate: LocalDate`
- `endDate: LocalDate?`

정책

- `startDate`는 null 불가
- `endDate`가 있으면 `startDate` 이상이어야 함
- `endDate` 누락은 무기한 반복 의미
- 과거 `interval` 필드는 제거됨

### 3.3 Execution

`Execution`은 `Deadline`과 분리된 독립 엔티티다.

필드(핵심)

- `deadline`
- `scheduledDate: LocalDate`
- `status: IN_PROGRESS | PAUSED | DONE`
- `completedAt: LocalDateTime?`

상태 전이

- `IN_PROGRESS -> PAUSED`
- `PAUSED -> IN_PROGRESS`
- `IN_PROGRESS/PAUSED -> DONE`
- `DONE` 이후 추가 전이는 예외

---

## 4. API 현행 스냅샷

### 4.1 Auth

- `POST /api/auth/login`
- `POST /api/auth/reissue`
- `POST /api/auth/logout`

로그아웃은 body 없는 호출도 허용하고, refresh token이 전달되면 서버에서 폐기한다.

### 4.2 User

- `POST /api/users/register`
- `GET /api/users/me`
- `GET /api/users/{id}`

### 4.3 Deadline

- `POST /api/deadlines`
- `GET /api/deadlines`
- `GET /api/deadlines/{id}`
- `PATCH /api/deadlines/{id}` (제목 수정)
- `PATCH /api/deadlines/{id}/recurrence`
- `DELETE /api/deadlines/{id}`

요청/응답 포인트

- `dueDate`는 날짜 포맷(`yyyy-MM-dd`)
- `startDate/endDate`는 원래부터 날짜 포맷
- `RECURRING` 등록 시 `startDate` 미입력은 서버가 `LocalDate.now(clock)`로 보정
- 목록 조회는 정렬 파라미터 지원: `sortBy`, `direction`

### 4.4 Execution

- `GET /api/executions/{executionId}`
- `GET /api/executions/deadline/{deadlineId}`
- `GET /api/executions?startDate=...&endDate=...`
- `PATCH /api/executions/{executionId}/done`
- `PATCH /api/executions/{executionId}/paused`
- `PATCH /api/executions/{executionId}/resume` (alias `/in-progress`도 허용)

---

## 5. 스케줄러/시간 정책

시간 처리 정책은 `Clock` 주입 기반으로 통일했다.

- 애플리케이션 타임존: `app.time-zone` (기본 `Asia/Seoul`)
- `TimeConfig`에서 시스템 default timezone 초기화 + `Clock` 빈 제공
- 예외 응답 timestamp, JWT 생성/검증 시각, 스케줄러 시각 계산을 `Clock` 기준으로 처리

스케줄러

- 매일 실행 생성 (`DailyExecutionScheduler` -> `ExecutionGenerationService`)
- ONE_TIME 연체 자동 완료 (`OverdueExecutionCompletionService`)
  - 기준: `dueDate < 오늘(LocalDate)`
- 만료 리프레시 토큰 정리 (`RefreshTokenCleanupScheduler`)

---

## 6. 최근 반영된 핵심 변경

- 기한 `description` 필드 제거 (도메인/DTO/API/테스트 전파)
- `Deadline.dueDate`를 `LocalDateTime -> LocalDate`로 변경
- ONE_TIME 연체 조회를 날짜 기준(`dueDate < today`)으로 변경
- RecurrenceRule 임베디드 컬럼 nullable 제약 정리로 ONE_TIME 저장 충돌 해소
- 문서(`API_SPEC.md`, `REVIEW.md`)를 현재 구현과 동기화

---

## 7. 품질 관점 요약

강점

- 도메인 규칙이 엔티티/값 객체 내부에 집중되어 일관성 높음
- 시간 의존 로직을 `Clock`으로 통일해 테스트 안정성 확보
- 인증 흐름(login/reissue/logout)과 토큰 회전 정책이 명확함
- 테스트 스위트가 도메인/애플리케이션/프레젠테이션/인프라 전 계층에 분포

유의사항

- API 스펙 문서와 코드가 다시 어긋나지 않도록 변경 시 동시 업데이트 필요
- `ddl-auto` 전략이 운영 DB에서는 제약 변경을 자동 반영하지 않을 수 있어 마이그레이션 관리 필요

---

## 8. 결론

현재 코드는 기한/실행 관리 도메인의 핵심 규칙(날짜 중심 정책, 상태 전이, 소유권 검증, 자동 생성/완료)을 안정적으로 구현한 상태다.
특히 최근 변경으로 Deadline 모델이 더 단순해졌고(`description` 제거, 날짜 타입 통일), 시간대/시각 관련 일관성도 개선되었다.
