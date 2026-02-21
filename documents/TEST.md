# Test Report

## 1. Naming Convention Check
- 테스트 메서드명 강제 규칙(ArchUnit)은 현재 없음.
- 요청 반영: 도메인/application/presentation/infrastructure 신규 테스트의 `@Test` 메서드명을 모두 영어 **camelCase**로 통일.

## 2. Clock Usage Notes
- 도메인 테스트는 시간을 파라미터로 받는 메서드 중심(`markAsDone(completedAt)`, `verifyNotExpired(now)` 등)이라 `Clock` 주입보다 고정 시각 전달이 더 결정적.
- 도메인 테스트에서 `LocalDateTime.now()` 사용 제거 완료.
- application/infrastructure는 실제로 "현재 시각 계산" 책임이 있는 계층이므로, 필요한 곳은 `Clock` 또는 현재 시각 호출 경로를 테스트로 검증.

## 3. Domain Tests (Tightened)
추가/강화:
- `BaseEntityTest` 신규: delete/restore/isDeleted/null 입력
- `DeadlineDomainTest` 강화: null 입력, update 실패 후 상태 보존, ownership 예외 메시지, rule exception factory 메시지
- `RecurrenceRuleTest` 강화: interval 음수/0, endDate 경계값, 예외 메시지
- `ExecutionTest` 강화: 상태 전이(PENDING/DONE/DELAYED), 예외 메시지
- `RefreshTokenTest` 강화: 만료 경계/이전/null, ownership null, 예외 메시지
- `UserTest` 강화: 접근 제어(본인/ADMIN/타인), null 인자, 예외 메시지

실행 결과:
- Domain test suites: 6
- Domain test cases: 77
- Failures: 0
- Errors: 0

## 4. Application Tests (New)
신규 테스트 클래스:
- `src/test/java/com/dochiri/kihan/application/user/command/RegisterUserServiceTest.java`
- `src/test/java/com/dochiri/kihan/application/auth/command/UserAuthenticationServiceTest.java`
- `src/test/java/com/dochiri/kihan/application/auth/command/IssueRefreshTokenServiceTest.java`
- `src/test/java/com/dochiri/kihan/application/auth/command/RevokeTokenServiceTest.java`
- `src/test/java/com/dochiri/kihan/application/auth/facade/LoginFacadeTest.java`
- `src/test/java/com/dochiri/kihan/application/auth/facade/ReissueTokenFacadeTest.java`
- `src/test/java/com/dochiri/kihan/application/execution/command/CreateExecutionServiceTest.java`
- `src/test/java/com/dochiri/kihan/application/execution/command/MarkExecutionAsDoneServiceTest.java`
- `src/test/java/com/dochiri/kihan/application/execution/command/MarkExecutionAsDelayedServiceTest.java`
- `src/test/java/com/dochiri/kihan/application/deadline/command/RegisterDeadlineServiceTest.java`
- `src/test/java/com/dochiri/kihan/application/deadline/command/UpdateDeadlineServiceTest.java`
- `src/test/java/com/dochiri/kihan/application/deadline/command/DeleteDeadlineServiceTest.java`
- `src/test/java/com/dochiri/kihan/application/deadline/query/DeadlineQueryServiceTest.java`

핵심 검증 포인트:
- 인증 성공/실패 분기
- 로그인 시 lastLoginAt 갱신 + refresh token 발급 커맨드 전달
- 재발급 성공 시 refresh token rotate/save, 소유자 불일치 예외
- 실행 생성 중복 방지/ID 반환
- 실행 완료/지연 상태 전이 서비스 동작
- 사용자 등록 중복 이메일 방지 + 비밀번호 인코딩
- 기한 등록 시 유효성 검증/저장/ID 반환
- 기한 수정 및 완료/삭제 처리 시 `Clock` 기준 시각 반영
- 기한 단건/목록 조회의 DTO 매핑 정확성

실행 결과:
- Application test suites: 13
- Application test cases: 23
- Failures: 0
- Errors: 0

## 5. Infrastructure Tests (New)
신규 테스트 클래스:
- `src/test/java/com/dochiri/kihan/infrastructure/security/jwt/RefreshTokenVerifierTest.java`
- `src/test/java/com/dochiri/kihan/infrastructure/security/jwt/JwtTokenGeneratorTest.java`
- `src/test/java/com/dochiri/kihan/infrastructure/security/jwt/JwtProviderTest.java`
- `src/test/java/com/dochiri/kihan/infrastructure/security/jwt/JwtAuthenticationConverterTest.java`
- `src/test/java/com/dochiri/kihan/infrastructure/security/jwt/JwtAuthenticationFilterTest.java`
- `src/test/java/com/dochiri/kihan/infrastructure/security/cookie/CookieProviderTest.java`
- `src/test/java/com/dochiri/kihan/infrastructure/security/audit/AuditorAwareImplTest.java`
- `src/test/java/com/dochiri/kihan/infrastructure/scheduler/RefreshTokenCleanupSchedulerTest.java`
- `src/test/java/com/dochiri/kihan/infrastructure/scheduler/DailyExecutionSchedulerTest.java`

핵심 검증 포인트:
- JWT 파싱/카테고리 검증/예외 변환
- 필터의 인증 세팅/무토큰 경로/예외 전파
- 쿠키 속성(domain/sameSite/httpOnly/secure/maxAge)
- 감사 주체(SYSTEM vs JwtPrincipal userId)
- 스케줄러가 서비스 호출을 정확히 위임하는지

실행 결과:
- Infrastructure test suites: 9
- Infrastructure test cases: 23
- Failures: 0
- Errors: 0

## 6. Presentation Tests (New)
신규 테스트 클래스:
- `src/test/java/com/dochiri/kihan/presentation/auth/AuthControllerTest.java`
- `src/test/java/com/dochiri/kihan/presentation/user/UserControllerTest.java`
- `src/test/java/com/dochiri/kihan/presentation/deadline/DeadlineControllerTest.java`
- `src/test/java/com/dochiri/kihan/presentation/execution/ExecutionControllerTest.java`

핵심 검증 포인트:
- Auth: 로그인/재발급/로그아웃 성공 흐름, 쿠키 처리 호출, 인증 실패(401), 입력 검증 실패(400)
- User: 회원가입 성공/검증 실패/중복 이메일(409), 내 정보 조회, 사용자 조회 권한 오류(403)
- Deadline: 등록(ONE_TIME/RECURRING) 201+Location, 단건/목록 조회, 수정/삭제 204, not found(404)
- Execution: 단건/기한별/기간별 조회, done/delayed 204, already completed 충돌(409), 날짜 파라미터 타입 오류 처리(500)

실행 결과:
- Presentation test suites: 4
- Presentation test cases: 27
- Failures: 0
- Errors: 0

## 7. Full Run
실행 명령:
```bash
./gradlew test
```

결과:
- BUILD SUCCESSFUL
- 신규 추가된 application/presentation/infrastructure 테스트 포함 전체 통과
