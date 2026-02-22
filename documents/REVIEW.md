# 스케줄링 조회 방식 및 페이징 처리 보고서 (2026-02-21)

## 1. 목적

프론트엔드에서 일정/실행 상태를 계속 조회할 때 배터리 소모를 줄이기 위해, 주기적 폴링 대신 `SSE` 또는 `WebSocket` 기반 갱신 방식 도입 가능성을 검토하고 페이징 전략을 제안한다.

## 2. 결론 요약

- **채택안: SSE 기본 + 폴링 fallback**
- 평상시에는 SSE로 변경 이벤트를 수신하고, SSE 불가/불안정 상황에서만 폴링으로 자동 전환한다.
- 목록 조회는 **Offset Pagination(page/size)** 으로 설계하고, 실시간 이벤트는 목록 전체 재조회 대신 **부분 갱신(invalidate + patch)** 전략을 쓴다.

## 3. 방식 비교

### 3.1 Polling

- 장점
- 구현 단순, 인프라 추가 부담이 작다.
- 단점
- 변경이 없어도 주기적으로 네트워크/라디오를 깨우므로 모바일 배터리 효율이 낮다.
- 짧은 주기일수록 배터리/트래픽 부담이 급격히 커진다.
- 긴 주기일수록 UI 최신성(지연)이 떨어진다.

### 3.2 SSE (Server-Sent Events)

- 장점
- HTTP 기반 단방향 스트림이라 서버/클라이언트 구현이 비교적 단순하다.
- 변경 이벤트가 있을 때만 푸시하므로 폴링 대비 배터리 효율이 좋다.
- 자동 재연결, `Last-Event-ID` 기반 유실 복구 패턴 적용이 쉽다.
- 단점
- 본질적으로 단방향(클라이언트 -> 서버 실시간 상호작용은 별도 API 필요)이다.
- 일부 프록시/로드밸런서의 idle timeout 설정 튜닝이 필요하다.

### 3.3 WebSocket

- 장점
- 완전 양방향 통신, 기능 확장성(실시간 협업/명령형 이벤트)에 유리하다.
- 단점
- 연결/세션/스케일아웃 운영 복잡도가 SSE보다 높다.
- 현재 요구가 단방향 알림 중심이면 오버엔지니어링 가능성이 있다.

## 4. 권장안

### 4.1 실시간 전송

- 1안(권장): **SSE 기본 + 폴링 fallback**
- 앱 활성 상태에서만 SSE 연결 유지
- 백그라운드 전환/화면 비활성 시 연결 해제
- 연결 실패 시 점진적 백오프 후 재연결
- 2안: 향후 양방향 요구가 확정되면 WebSocket으로 단계 전환

### 4.2 이벤트 설계

- 이벤트 타입 예시
- `deadline.created`
- `deadline.updated`
- `deadline.deleted`
- `execution.updated`
- 이벤트 페이로드 원칙
- 전체 목록 전달 금지(배터리/트래픽 낭비)
- 변경 엔티티 최소 필드 + `updatedAt` + `version` 전달
- 클라이언트 반영
- 현재 보이는 페이지에 해당 항목이 있으면 patch update
- 보이지 않는 항목이면 캐시 무효화만 표시(배지/새로고침 힌트)

## 5. 페이징 처리 제안

### 5.1 Offset Pagination(page/size) 채택

- 이유
- 구현/디버깅/운영이 단순하고 프론트 상태 관리가 직관적이다.
- 화면 번호 기반 이동(예: 1, 2, 3 페이지)이 쉬워 UX 설명이 명확하다.
- 기존 API/DB 쿼리 패턴과 맞추기 쉽다.
- 보완점
- 실시간 변경 시 페이지 밀림(중복/누락) 가능성이 있으므로 보정 규칙을 함께 둔다.
- 정렬은 반드시 고정한다: `createdAt DESC, id DESC` (tie-breaker 포함). 현재 `DeadlineQueryService`는 단일 필드 정렬만 지원하므로 페이징 구현 시 `id` tie-breaker를 추가해야 한다.

### 5.2 API 예시

```http
GET /api/deadlines?page=0&size=20&sortBy=CREATED_AT&direction=DESC
```

```json
{
  "items": [...],
  "pageInfo": {
    "page": 0,
    "size": 20,
    "totalElements": 183,
    "totalPages": 10,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### 5.3 실시간 이벤트와 페이징 결합 규칙

- 첫 페이지(최신순)는 이벤트를 즉시 반영한다.
- 2페이지 이상은 즉시 재정렬하지 않고 캐시 invalidate만 수행한다.
- 사용자 액션(새로고침/재진입) 시 해당 페이지만 재조회한다.
- 삭제 이벤트 수신 시 현재 페이지에서 항목 제거 후 `hasNext`면 1건 추가 fetch로 빈자리 보정한다.
- 생성/수정 이벤트로 정렬 위치가 바뀌면 페이지 상단에 `새 항목 N개` 배지를 노출하고 수동 새로고침으로 동기화한다.

## 6. 모바일 배터리 최적화 체크리스트

- 화면 활성 시에만 SSE 연결
- 앱 백그라운드 진입 시 연결 종료
- heartbeat 주기 최소화(필요 시에만)
- 재연결 백오프(예: 1s, 2s, 4s ... 최대 30s)
- 데이터 절약 모드/저전력 모드에서 실시간 끄기 옵션 제공

## 7. 단계별 적용 순서

1. 목록 API를 Offset Pagination으로 정리
2. SSE 엔드포인트 추가 및 이벤트 스키마 고정
3. 프론트에 실시간 patch/invalidate 반영 로직 적용
4. 폴링 fallback 및 백오프/수명주기 제어 적용
5. 트래픽/배터리/지연 지표 측정 후 튜닝

## 8. 의사결정

- **SSE 기본 + 폴링 fallback + Offset Pagination** 채택

## 9. 구현 상세

### 9.1 서버 API

- 목록 조회(기존 유지)
- `GET /api/deadlines?page={n}&size={m}&sortBy=CREATED_AT&direction=DESC`
- SSE 구독(신규)
- `GET /api/deadlines/stream`
- 헤더: `Accept: text/event-stream`
- 인증: Access Token(`Authorization: Bearer`) 사용. 웹 환경에서 `EventSource`를 쓰는 경우 `withCredentials`와 refresh token 쿠키 기반 재인증 흐름을 함께 사용한다.
- 재연결 복구
- 클라이언트는 마지막 수신 ID를 `Last-Event-ID`로 전달
- 서버는 가능하면 해당 ID 이후 이벤트부터 재전송, 불가능하면 `resync_required` 이벤트 전송

### 9.2 SSE 이벤트 포맷

```text
id: 981233
event: deadline.updated
data: {"deadlineId":1842,"updatedAt":"2026-02-21T13:04:11Z","version":7}
```

- 필수 규칙
- `id`: 단조 증가(복구 기준)
- `event`: `deadline.created|updated|deleted`, `execution.updated`, `resync_required`
- `data`: 최소 필드만 전달(전체 목록 금지)

### 9.3 클라이언트 상태 머신

- 상태
- `SSE_CONNECTING` -> `SSE_ACTIVE` -> `POLLING_ACTIVE` 중 하나 유지
- 전환 규칙
- 앱 포그라운드 진입: SSE 연결 시도
- SSE 연결 성공: 폴링 중지
- SSE 에러 누적(예: 3회 연속 실패): 폴링 모드 전환
- 폴링 중 주기적으로 SSE 재시도(예: 60초마다 1회)
- 앱 백그라운드: SSE/폴링 모두 중지

### 9.4 폴링 fallback 정책

- 폴링 엔드포인트
- `GET /api/deadlines?page=0&size=20&sortBy=CREATED_AT&direction=DESC`
- 주기
- 기본 30초, 사용자 상호작용 중 15초, 저전력 모드 60초
- 조건부 요청
- 목록 데이터에 `ETag` 적용 시 매 요청마다 해시 계산이 필요하고 변경이 잦으면 캐시 히트율이 낮으므로, `Last-Modified` / `If-Modified-Since` 기반이 더 실용적이다. 변경 없으면 `304 Not Modified` 반환.

### 9.5 화면 반영 규칙

- 현재 페이지(페이지 0)
- `created/updated/deleted` 이벤트 즉시 patch
- 1페이지 이상
- 데이터 직접 재정렬하지 않고 invalidate 플래그만 설정
- 사용자가 페이지 재진입/새로고침할 때 재조회
- `resync_required` 수신
- 현재 탭의 목록 1회 전체 재조회 후 마지막 이벤트 ID 갱신

### 9.6 백엔드 운영 포인트

- 커넥션 관리
- 사용자별 동시 SSE 연결 1개 제한(중복 탭은 최신 연결만 유지 또는 탭별 허용 정책 명시)
- heartbeat
- 20~30초 주기로 comment ping(`:keepalive`) 전송해 중간 프록시 idle timeout 회피
- 타임아웃
- 인프라(LB/Nginx) read timeout을 heartbeat보다 길게 설정

### 9.7 모니터링 지표

- `sse_active_connections`
- `sse_reconnect_count`
- `sse_to_polling_fallback_count`
- `polling_requests_per_user`
- `event_delivery_lag_ms`(이벤트 생성~수신)
- `resync_required_count`

### 9.8 적용 순서(구현 태스크)

1. `GET /api/deadlines/stream` 추가 및 이벤트 발행 지점(deadline/execution 변경 트랜잭션 후) 연결
2. 이벤트 ID 발급/보관 정책 확정(메모리 또는 Redis) 및 `Last-Event-ID` 복구 구현
3. 프론트 SSE 구독 훅/서비스 구현 + 페이지 0 patch 반영
4. SSE 실패 누적 시 폴링 전환 로직 및 백오프 적용
5. `Last-Modified` / `If-Modified-Since` 기반 조건부 폴링 적용
6. 모니터링 대시보드/알람(폴백 급증, 지연 증가) 설정

## 10. 후속 수정 사항 (2026-02-22)

- 단건/반복 판별 단순화
- 비즈니스 로직에서 `ONE_TIME` 값 의존을 줄이고, `recurrenceRule` 유무(반복 여부)와 `dueDate` 유무(단건 여부)로 처리하도록 정리했다.
- 등록 요청에서 `type`은 선택값으로 완화했다. `pattern`이 있으면 반복, 없으면 단건으로 자동 판별한다.
- 마감 지난 단건 자동 완료 누락 보정
- 단건 실행 생성 조건을 `dueDate == today`에서 `dueDate <= today`로 확장해, 서버 중단 등으로 생성이 누락된 단건 실행도 이후 스케줄에서 보정 생성한다.
- 보정 생성 시 `scheduledDate`는 현재 날짜가 아니라 실제 `dueDate`를 사용한다.
- 연체 자동 완료 쿼리는 `type == ONE_TIME` 조건 대신 `dueDate is not null` 조건으로 변경해 단건 판별 누락을 줄였다.
- 실행 생성 스케줄은 일 1회에서 매 분으로 변경해 누락 실행 보정을 빠르게 반영한다.
- 수동 완료 안정성을 위해 `PATCH /api/executions/deadline/{deadlineId}/done`를 추가했다. 실행이 없으면 생성 후 즉시 완료 처리한다.
