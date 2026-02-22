# KIHAN 코드 리뷰 (2026-02-22)

## 범위
- 인증(JWT/쿠키), 기한(Deadline), 실행(Execution), 예외 매핑, 스케줄러, SSE 브로커

## 총평
- 도메인 중심 구조, 테스트 기반 개발, 인증 분리 전략은 양호하다.
- 최근 리팩토링으로 `Deadline` 등록 모델이 단순해졌지만, 일부 호환 코드와 운영 설정은 정리 여지가 남아 있다.

## 주요 이슈 (우선순위 순)

### P1. Deadline 검증 규칙 이중화
- 위치: `src/main/java/com/dochiri/kihan/domain/deadline/Deadline.java`
- 현황: `register(userId, title, dueDate, recurrenceRule)`와 `register(userId, title, type, dueDate, recurrenceRule)`가 동시에 존재하며 규칙 검증이 분산돼 있다.
- 리스크: 단건/반복 판별 규칙 변경 시 두 경로가 어긋날 수 있다.
- 권장 조치:
1. 구 시그니처(타입 포함) 제거 또는 `@Deprecated` 처리 후 제거 계획 수립
2. 검증 진입점을 `validate(dueDate, recurrenceRule)` 한 곳으로 강제

### P1. 실행 상태 변경 서비스의 중복 방어 로직
- 위치:
1. `src/main/java/com/dochiri/kihan/application/execution/command/MarkExecutionAsDoneService.java`
2. `src/main/java/com/dochiri/kihan/application/execution/command/MarkExecutionAsInProgressService.java`
3. `src/main/java/com/dochiri/kihan/application/execution/command/MarkExecutionAsPausedService.java`
- 현황: 세 서비스가 동일한 흐름(조회 -> 삭제된 Deadline 검사 -> 소유권 검사 -> 상태 변경)을 반복한다.
- 리스크: 검사 순서/예외 정책이 분기마다 달라질 가능성.
- 권장 조치:
1. 공통 헬퍼(예: `loadActiveOwnedExecution`)로 조회+검증 통합
2. 리포지토리에서 `deadline.deletedAt is null` 조건 포함한 조회 메서드 제공 검토

### P2. ExceptionStatusMapper 미매핑 예외 관측성 부족
- 위치: `src/main/java/com/dochiri/kihan/presentation/common/exception/ExceptionStatusMapper.java`
- 현황: 미매핑 예외는 500으로 반환되지만 매핑 실패 로그가 없다.
- 리스크: 운영 환경에서 신규 예외 누락 탐지가 늦어진다.
- 권장 조치:
1. 매핑 실패 시 WARN 로그(예외 타입/메시지/path) 추가

### P2. RecurrencePattern에 규칙 계산 책임 부재
- 위치:
1. `src/main/java/com/dochiri/kihan/domain/deadline/RecurrencePattern.java`
2. `src/main/java/com/dochiri/kihan/application/execution/scheduler/ExecutionGenerationService.java`
- 현황: 반복 주기 계산이 서비스의 switch문에 고정돼 있다.
- 리스크: 패턴 확장 시 서비스 비대화.
- 권장 조치:
1. `RecurrencePattern`에 계산 메서드 위임(전략/템플릿 패턴)

### P3. 스케줄러 주기 운영 튜닝 포인트
- 위치:
1. `src/main/java/com/dochiri/kihan/infrastructure/scheduler/DailyExecutionScheduler.java`
2. `src/main/java/com/dochiri/kihan/infrastructure/scheduler/OverdueExecutionCompletionScheduler.java`
- 현황: 두 스케줄러 모두 매 분 실행(`0 * * * * *`).
- 리스크: 데이터 규모 증가 시 전체 스캔 비용 증가.
- 권장 조치:
1. 실행 시간/처리량 메트릭 계측
2. 부하 구간에서 주기 조정 또는 변경분 기반 처리 도입

### P3. SSE 백로그 상수 하드코딩
- 위치: `src/main/java/com/dochiri/kihan/infrastructure/realtime/DeadlineStreamBroker.java`
- 현황: `MAX_EVENT_BACKLOG = 200` 고정.
- 리스크: 운영 환경별 재연결 패턴에 대응하기 어렵다.
- 권장 조치:
1. 설정값 외부화(`application.yml` + `@ConfigurationProperties`)

## 테스트 보완 제안
1. `Deadline.register` 구 시그니처 제거 시 도메인 테스트 일괄 정리
2. 실행 상태 변경 공통 로더 도입 시 서비스 3종 회귀 테스트 추가
3. 예외 매핑 실패 로그 동작 검증 테스트 추가
