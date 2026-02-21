# KIHAN 코드 리뷰 (남은 보류 항목)

## 반영 현황 (2026-02-21)

- 완료 항목: 기존 리뷰의 버그/설계 개선 및 `DateRangeQuery` 검증은 반영 완료
- 현재 문서: 미반영(보류) 항목만 유지

## 1. 미구현 / 누락 기능

### 1.1 `markAsCompleted()` — API 미노출 및 의미 중복

`UpdateDeadlineService.markAsCompleted()`가 구현되어 있지만 컨트롤러 엔드포인트가 없다.

또한 현재 `DeleteDeadlineService.execute()`와 내부 동작이 사실상 동일하다.

| 항목 | `DeleteDeadlineService.execute()` | `markAsCompleted()` |
|------|---|---|
| 내부 호출 | `deadline.delete(now)` | `deadline.markAsCompleted(now)` |
| API | `DELETE /api/deadlines/{id}` | 없음 |

의미를 분리할지(완료 vs 삭제), 통합할지 정책 결정을 먼저 해야 한다.

### 1.2 RecurrenceRule 수정 API 없음

현재 `Deadline.update()`는 title/description만 수정 가능하고 반복 규칙(`pattern`, `interval`, `startDate`, `endDate`)은 수정할 수 없다.

반복 규칙 변경 시 기존 생성 실행(Execution) 처리 정책(유지/재생성/부분 재생성)까지 포함해 설계가 필요하다.

### 1.3 `DELAYED -> PENDING` 복원 경로 없음

현재 상태 전이:

```text
PENDING -> DONE    O
PENDING -> DELAYED O
DELAYED -> DONE    O
DELAYED -> PENDING X
DONE -> PENDING    X
```

실수로 지연 처리한 실행을 복원하는 요구가 있다면 별도 상태 전이 규칙/권한 정책과 함께 API 추가가 필요하다.

### 1.4 페이지네이션 없음

`deadline`/`execution` 조회 API가 모두 전체 조회로 동작한다.

- `GET /api/deadlines`
- `GET /api/executions`
- `GET /api/executions/deadline/{deadlineId}`

데이터 증가 시 응답 크기/성능 문제가 예상되므로 `page`, `size`, `sort`(또는 cursor) 도입이 필요하다.

## 2. 우선순위

| 항목 | 심각도 |
|------|--------|
| RecurrenceRule 수정 API 없음 | 높음 |
| `markAsCompleted()` 의미 중복 + API 미노출 | 중간 |
| `DELAYED -> PENDING` 복원 없음 | 중간 |
| 페이지네이션 없음 | 낮음 |
