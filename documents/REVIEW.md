# KIHAN 코드 리뷰 정리 (2026-02-21)

1. `Execution` 상태는 `IN_PROGRESS`, `PAUSED`, `DONE`으로 통일했고 재개 API(`PATCH /api/executions/{id}/in-progress`)를 추가했다.
2. `API_SPEC.md`는 실행 상태, 엔드포인트, 반복 규칙(`interval` 제거, `endDate` 미입력 시 무기한) 기준으로 갱신했다.
3. 시간 처리는 `Clock`과 `TimeProperties(app.time-zone)` 기반으로 정리해 예외 응답 시각과 JPA Auditing 시각 경로를 일치시켰다.
4. 예외 및 접근 일관성은 `RefreshTokenNotFoundException` 메시지 수정, `DeleteDeadlineService` 트랜잭션 정리, 삭제된 Deadline 연계 Execution 접근 차단으로 반영했다.
5. 일정 조회는 `GET /api/deadlines?sortBy=CREATED_AT|DUE_DATE|TITLE&direction=ASC|DESC` 정렬 기능을 추가했고 기본 정렬은 `CREATED_AT DESC`다.
6. 반복 규칙 수정 API `PATCH /api/deadlines/{id}/recurrence`를 추가했다.
7. Swagger Basic 인증 계정은 인메모리 하드코딩을 제거하고 `swagger.auth.username/password/role` 프로퍼티로 외부화했다.
8. `InactiveUserException`은 도메인/매퍼/테스트에서 정리했다.
9. `interval` 제거는 의도된 정책으로 유지한다.
