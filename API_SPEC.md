# Kihan Frontend API Spec

## 1) 공통

- Base URL: `http://{host}:{port}`
- API Prefix: `/api`
- Content-Type: `application/json`
- 인증 방식: `Authorization: Bearer {accessToken}`
- 공개 엔드포인트:
  - `POST /api/users/register`
  - `POST /api/auth/login`
  - `POST /api/auth/reissue`
- 보호 엔드포인트: 위 3개 제외 전부

## 2) 인증(Auth)

### 2.1 로그인

- `POST /api/auth/login`
- Auth: 불필요

요청:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

유효성:

- `email`: 이메일 형식
- `password`: 8~20자

응답 `200`:

```json
{
  "userId": 1,
  "role": "USER",
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

### 2.2 토큰 재발급

- `POST /api/auth/reissue`
- Auth: 불필요

요청:

```json
{
  "refreshToken": "eyJ..."
}
```

유효성:

- `refreshToken`: 필수, 공백 불가

응답 `200`:

```json
{
  "userId": 1,
  "role": "USER",
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

### 2.3 로그아웃

- `POST /api/auth/logout`
- Auth: 불필요

요청(선택):

```json
{
  "refreshToken": "eyJ..."
}
```

응답:

- `204 No Content`

동작:

- body에 `refreshToken`이 있으면 서버에서 해당 토큰 폐기
- body가 없거나 `refreshToken`이 없으면 서버는 토큰 폐기 없이 `204` 반환

## 3) 사용자(User)

### 3.1 회원가입

- `POST /api/users/register`
- Auth: 불필요

요청:

```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

유효성:

- `email`: 필수, 이메일 형식
- `password`: 필수, 8~20자
- `name`: 필수, 2~10자

응답 `200`:

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

응답 `200`:

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

응답 `200`:

```json
{
  "id": 2,
  "email": "other@example.com",
  "name": "김철수",
  "role": "USER"
}
```

## 4) 기한(Deadlines)

### 4.1 기한 등록

- `POST /api/deadlines`
- Auth: 필요

요청(ONE_TIME):

```json
{
  "title": "프로젝트 제출",
  "description": "최종 보고서 제출",
  "type": "ONE_TIME",
  "dueDate": "2027-12-31T23:59:59",
  "pattern": null,
  "interval": null,
  "startDate": null,
  "endDate": null
}
```

요청(RECURRING):

```json
{
  "title": "주간 회의",
  "description": "팀 주간 회의",
  "type": "RECURRING",
  "dueDate": null,
  "pattern": "WEEKLY",
  "interval": null,
  "startDate": "2027-01-01",
  "endDate": null
}
```

유효성:

- `type=RECURRING`인 경우 `pattern`, `startDate`는 필수
- `interval`은 선택값이며, 누락 시 기본 `1`로 처리
- `interval`이 주어졌다면 `1` 이상이어야 함
- `endDate`는 선택값이며, 누락 시 무기한 반복

응답:

- `201 Created`
- `Location` 헤더: `/api/deadlines/{deadlineId}`

### 4.2 기한 단건 조회

- `GET /api/deadlines/{id}`
- Auth: 필요

응답 `200`:

```json
{
  "id": 1,
  "title": "프로젝트 제출",
  "description": "최종 보고서 제출",
  "type": "ONE_TIME",
  "dueDate": "2027-12-31T23:59:59",
  "recurrenceRule": null,
  "createdAt": "2026-02-21T15:00:00"
}
```

### 4.3 기한 목록 조회

- `GET /api/deadlines`
- Auth: 필요

응답 `200`: `DeadlineResponse[]`

### 4.4 기한 수정

- `PATCH /api/deadlines/{id}`
- Auth: 필요

요청:

```json
{
  "title": "프로젝트 최종 제출",
  "description": "보고서와 코드 제출"
}
```

응답:

- `204 No Content`

### 4.5 기한 삭제

- `DELETE /api/deadlines/{id}`
- Auth: 필요

응답:

- `204 No Content`

## 5) 실행(Executions)

### 5.1 실행 단건 조회

- `GET /api/executions/{executionId}`
- Auth: 필요

응답 `200`:

```json
{
  "id": 1,
  "deadlineId": 1,
  "scheduledDate": "2026-02-14",
  "status": "PENDING",
  "completedAt": null
}
```

### 5.2 기한별 실행 목록 조회

- `GET /api/executions/deadline/{deadlineId}`
- Auth: 필요

응답 `200`: `ExecutionResponse[]`

### 5.3 기간별 실행 목록 조회

- `GET /api/executions?startDate=2026-02-01&endDate=2026-02-28`
- Auth: 필요

유효성:

- `startDate`는 `endDate`보다 늦을 수 없음
- `startDate > endDate`이면 `400 Bad Request` (`InvalidExecutionDateRangeException`)

응답 `200`: `ExecutionResponse[]`

### 5.4 실행 완료 처리

- `PATCH /api/executions/{executionId}/done`
- Auth: 필요

응답:

- `204 No Content`

### 5.5 실행 지연 처리

- `PATCH /api/executions/{executionId}/delayed`
- Auth: 필요

응답:

- `204 No Content`

## 6) 공통 응답 모델

### 6.1 UserResponse

```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "role": "USER"
}
```

### 6.2 AuthResponse

```json
{
  "userId": 1,
  "role": "USER",
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

### 6.3 DeadlineResponse

```json
{
  "id": 1,
  "title": "주간 회의",
  "description": "팀 주간 회의",
  "type": "RECURRING",
  "dueDate": null,
  "recurrenceRule": {
    "pattern": "WEEKLY",
    "interval": 1,
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

## 7) 에러 응답

### 7.1 Validation 에러 (`400`)

```json
{
  "title": "VALIDATION_FAILED",
  "status": 400,
  "detail": "입력값이 올바르지 않습니다.",
  "path": "/api/auth/login",
  "timestamp": "2026-02-21T15:10:00",
  "errors": [
    {
      "field": "password",
      "message": "size must be between 8 and 20"
    }
  ]
}
```

### 7.2 일반 비즈니스 에러 (`4xx/5xx`)

```json
{
  "status": 401,
  "detail": "이메일 또는 비밀번호가 올바르지 않습니다.",
  "timestamp": "2026-02-21T15:10:00",
  "exception": "InvalidCredentialsException",
  "path": "/api/auth/login"
}
```

## 8) Enum 값

- `role`: `USER`, `ADMIN`
- `deadline.type`: `ONE_TIME`, `RECURRING`
- `recurrenceRule.pattern`: `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`
- `execution.status`: `PENDING`, `DONE`, `DELAYED`

## 9) 프론트 토큰 처리 가이드

- 로그인 성공 시:
  - `accessToken`: 메모리 상태
  - `refreshToken`: 안전 저장소(Keystore/Keychain)
- 보호 API 호출 시 `Authorization: Bearer {accessToken}`
- `401` 발생 시 `POST /api/auth/reissue`로 재발급 후 재시도
- 로그아웃 시 `POST /api/auth/logout` 호출 후 클라이언트 저장 토큰 삭제
