# KIHAN 코드 리뷰 정리 (2026-02-21)

1. `Execution` 상태는 `IN_PROGRESS`, `PAUSED`, `DONE`으로 통일했고 재개 API를 추가했다. 컨트롤러는 `PATCH /api/executions/{id}/resume`과 `PATCH /api/executions/{id}/in-progress` 둘 다 매핑하며, `API_SPEC.md` 정식 경로는 `/resume`으로 기재됐다.
2. `API_SPEC.md`는 실행 상태, 기본 엔드포인트, 반복 규칙(`interval` 제거, `endDate` 미입력 시 무기한) 기준으로 갱신했다. 단, 기한 목록 정렬 파라미터(item 5)와 반복 규칙 수정 엔드포인트(item 6)는 아직 `API_SPEC.md`에 반영되지 않았다.
3. 시간 처리는 `Clock`과 `TimeProperties(app.time-zone)` 기반으로 정리해 예외 응답 시각과 JPA Auditing 시각 경로를 일치시켰다.
4. 예외 및 접근 일관성은 `RefreshTokenNotFoundException` 메시지 수정, `DeleteDeadlineService` 트랜잭션 정리, 삭제된 Deadline 연계 Execution 접근 차단으로 반영했다.
5. 일정 조회는 `GET /api/deadlines?sortBy=CREATED_AT|DUE_DATE|TITLE&direction=ASC|DESC` 정렬 기능을 추가했고 기본 정렬은 `CREATED_AT DESC`다. `API_SPEC.md` 4.3절에 파라미터 명세 추가 필요.
6. 반복 규칙 수정 API `PATCH /api/deadlines/{id}/recurrence`를 추가했다. `API_SPEC.md`에 해당 엔드포인트 추가 필요.
7. Swagger Basic 인증 계정은 인메모리 하드코딩을 제거하고 `swagger.auth.username/password/role` 프로퍼티로 외부화했다.
8. `InactiveUserException`은 도메인/매퍼/테스트에서 정리했다.
9. `interval` 제거는 의도된 정책으로 유지한다.
10. RECURRING 기한 등록 시 `startDate` 미입력이면 `Clock` 기반 오늘 날짜(`LocalDate.now(clock)`)로 자동 대체한다. `pattern`만 필수이며, 대체 로직은 `DeadlineRegisterRequest.toRecurrenceRule(Clock)`에서 처리한다.
11. Deadline `description` 필드는 도메인/DTO/API 요청·응답/컨트롤러/서비스/테스트 전반에서 제거했다. 기한 등록/조회/수정은 제목 중심으로 동작한다.
12. `RecurrenceRule` 임베디드 컬럼(`pattern`, `startDate`)의 JPA `nullable=false`를 제거해 ONE_TIME 저장 시 `start_date` 제약 충돌이 발생하지 않도록 정리했다.
13. Deadline `dueDate`는 `LocalDateTime`에서 `LocalDate`로 변경해 마감일에서 시간 정보를 제거했다. 요청/응답 예시와 테스트도 `yyyy-MM-dd` 기준으로 정리했다.
14. ONE_TIME 연체 조회는 날짜 기준(`dueDate < 오늘`)으로 변경해 마감일이 지난 실행만 자동 완료 대상으로 처리한다.
