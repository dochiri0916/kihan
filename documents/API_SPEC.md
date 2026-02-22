# Kihan Frontend API Spec

## 1) 공통

- Base URL: `http://{host}:{port}`
- API Prefix: `/api`
- Request Content-Type: `application/json`
- 인증 방식
- Access Token: `Authorization: Bearer {accessToken}`
- Refresh Token: HttpOnly Cookie `refreshToken`
- 공개 엔드포인트
- `POST /api/users/register`
- `POST /api/auth/login`
- `POST /api/auth/reissue`
- `POST /api/auth/logout`
- 보호 엔드포인트: 위 4개 제외 전부

---

## 2) 인증(Auth)

### 2.1 로그인

- `POST /api/auth/login`
- Auth: 불필요

요청

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

응답 `200`

```json
{
  "userId": 1,
  "role": "USER",
  "accessToken": "eyJ..."
}
```

응답 헤더

- `Set-Cookie: refreshToken={token}; HttpOnly; Path=/; Max-Age=1209600; SameSite=Lax`

### 2.2 토큰 재발급

- `POST /api/auth/reissue`
- Auth: 불필요
- 요청 바디 없음

요청 헤더/쿠키

- Cookie: `refreshToken={token}`

응답 `200`

```json
{
  "userId": 1,
  "role": "USER",
  "accessToken": "eyJ..."
}
```

응답 헤더

- `Set-Cookie: refreshToken={newToken}; HttpOnly; Path=/; Max-Age=1209600; SameSite=Lax`

### 2.3 로그아웃

- `POST /api/auth/logout`
- Auth: 불필요
- 요청 바디 없음

요청 헤더/쿠키(선택)

- Cookie: `refreshToken={token}`

응답

- `204 No Content`
- `Set-Cookie: refreshToken=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax`

---

## 3) 사용자(User)

### 3.1 회원가입

- `POST /api/users/register`
- Auth: 불필요

요청

```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

유효성

- `email`: 필수, 이메일 형식
- `password`: 필수, 8~20자
- `name`: 필수, 2~10자

응답 `200`

```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "role": "USER"
}
```

### 3.2 내 정보 조회

- `GET /api/users/me`
- Auth: 필요

응답 `200`

```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "role": "USER"
}
```

### 3.3 사용자 조회

- `GET /api/users/{id}`
- Auth: 필요

응답 `200`

```json
{
  "id": 2,
  "email": "other@example.com",
  "name": "김철수",
  "role": "USER"
}
```

---

## 4) 기한(Deadlines)

### 4.1 기한 등록

- `POST /api/deadlines`
- Auth: 필요

요청(단건)

```json
{
  "title": "프로젝트 제출",
  "dueDate": "2027-12-31",
  "pattern": null,
  "startDate": null,
  "endDate": null
}
```

요청(반복)

```json
{
  "title": "주간 회의",
  "dueDate": null,
  "pattern": "WEEKLY",
  "startDate": "2027-01-01",
  "endDate": "2027-12-31"
}
```

유효성

- `title`: 필수, 공백 불가
- `pattern == null`이면 단건으로 처리하며 `dueDate` 필수
- `pattern != null`이면 반복으로 처리하며 `dueDate`는 반드시 `null`
- `dueDate`와 `pattern`이 모두 `null`이면 `400`
- 반복에서 `startDate` 생략 시 서버가 `LocalDate.now(clock)`로 대체
- 반복에서 `endDate`는 선택

응답

- `201 Created`
- `Location: /api/deadlines/{deadlineId}`

### 4.2 기한 단건 조회

- `GET /api/deadlines/{id}`
- Auth: 필요

응답 `200`

```json
{
  "id": 1,
  "title": "프로젝트 제출",
  "type": "ONE_TIME",
  "dueDate": "2027-12-31",
  "recurrenceRule": null,
  "createdAt": "2026-02-21T15:00:00"
}
```

### 4.3 기한 목록 조회

- `GET /api/deadlines`
- Auth: 필요

쿼리 파라미터

- `page`: 기본 `0`
- `size`: 기본 `20`
- `sortBy`: `CREATED_AT`(기본), `DUE_DATE`, `TITLE`
- `direction`: `DESC`(기본), `ASC`

요청 헤더(선택)

- `If-Modified-Since: <RFC_1123_DATE_TIME>`

응답

- `200 OK` + `Last-Modified` + `DeadlinePageResponse`
- `304 Not Modified`

응답 `200` 예시

```json
{
  "items": [
    {
      "id": 1,
      "title": "프로젝트 제출",
      "type": "ONE_TIME",
      "dueDate": "2027-12-31",
      "recurrenceRule": null,
      "createdAt": "2026-02-21T15:00:00"
    }
  ],
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

### 4.4 기한/실행 변경 이벤트 구독(SSE)

- `GET /api/deadlines/stream`
- Auth: 필요
- `Accept: text/event-stream`
- 요청 헤더(선택): `Last-Event-ID`

이벤트 예시

```text
id: 981233
event: deadline.updated
data: {"deadlineId":1842,"updatedAt":"2026-02-21T13:04:11Z","version":7}
```

### 4.5 기한 수정

- `PATCH /api/deadlines/{id}`
- Auth: 필요

요청

```json
{
  "title": "새 제목"
}
```

응답

- `204 No Content`

### 4.6 반복 규칙 수정

- `PATCH /api/deadlines/{id}/recurrence`
- Auth: 필요

요청

```json
{
  "pattern": "WEEKLY",
  "startDate": "2027-01-01",
  "endDate": "2027-12-31"
}
```

응답

- `204 No Content`

### 4.7 기한 삭제

- `DELETE /api/deadlines/{id}`
- Auth: 필요

응답

- `204 No Content`

---

## 5) 실행(Executions)

### 5.1 실행 단건 조회

- `GET /api/executions/{executionId}`
- Auth: 필요

응답 `200`

```json
{
  "id": 10,
  "deadlineId": 1,
  "scheduledDate": "2026-02-21",
  "status": "DONE",
  "completedAt": "2026-02-21T10:30:00"
}
```

### 5.2 기한별 실행 목록 조회

- `GET /api/executions/deadline/{deadlineId}`
- Auth: 필요

응답 `200`

```json
[
  {
    "id": 10,
    "deadlineId": 1,
    "scheduledDate": "2026-02-21",
    "status": "IN_PROGRESS",
    "completedAt": null
  }
]
```

### 5.3 기간별 실행 목록 조회

- `GET /api/executions?startDate={yyyy-MM-dd}&endDate={yyyy-MM-dd}`
- Auth: 필요

유효성

- `startDate > endDate`이면 `400`

응답 `200`

```json
[
  {
    "id": 10,
    "deadlineId": 1,
    "scheduledDate": "2026-02-21",
    "status": "PAUSED",
    "completedAt": null
  }
]
```

### 5.4 실행 완료 처리

- `PATCH /api/executions/{executionId}/done`
- Auth: 필요

응답

- `204 No Content`

### 5.5 기한 기준 실행 완료 처리

- `PATCH /api/executions/deadline/{deadlineId}/done`
- Auth: 필요
- 실행이 없으면 생성 후 완료 처리

응답

- `204 No Content`

### 5.6 실행 중지 처리

- `PATCH /api/executions/{executionId}/paused`
- Auth: 필요

응답

- `204 No Content`

### 5.7 실행 재개 처리

- `PATCH /api/executions/{executionId}/resume`
- `PATCH /api/executions/{executionId}/in-progress`
- Auth: 필요

응답

- `204 No Content`

---

## 6) DTO 요약

### 6.1 AuthResponse

```json
{
  "userId": 1,
  "role": "USER",
  "accessToken": "eyJ..."
}
```

### 6.2 UserResponse

```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "role": "USER"
}
```

### 6.3 DeadlineResponse

```json
{
  "id": 1,
  "title": "주간 회의",
  "type": "RECURRING",
  "dueDate": null,
  "recurrenceRule": {
    "pattern": "WEEKLY",
    "startDate": "2027-01-01",
    "endDate": "2027-12-31"
  },
  "createdAt": "2026-02-21T15:00:00"
}
```

### 6.4 ExecutionResponse

```json
{
  "id": 10,
  "deadlineId": 1,
  "scheduledDate": "2026-02-21",
  "status": "DONE",
  "completedAt": "2026-02-21T10:30:00"
}
```

---

## 7) 에러 응답

공통 형식: RFC 7807 `ProblemDetail`

예시(Validation `400`)

```json
{
  "title": "VALIDATION_FAILED",
  "status": 400,
  "detail": "입력값이 올바르지 않습니다.",
  "path": "/api/auth/login",
  "timestamp": "2026-02-22T13:00:00Z",
  "errors": [
    {
      "field": "email",
      "message": "올바른 이메일 형식이어야 합니다"
    }
  ]
}
```

대표 상태 코드

- `400 Bad Request`: 검증 실패
- `401 Unauthorized`: 인증 실패/토큰 문제
- `403 Forbidden`: 권한 부족
- `404 Not Found`: 리소스 없음
- `409 Conflict`: 중복 등 충돌
- `500 Internal Server Error`: 서버 내부 오류

---

## 8) 열거형

- `deadline.type`: `ONE_TIME`, `RECURRING`
- `recurrenceRule.pattern`: `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`
- `execution.status`: `IN_PROGRESS`, `PAUSED`, `DONE`
