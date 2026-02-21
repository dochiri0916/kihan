# 모바일 앱 전환 - 쿠키 제거 및 인증 방식 변경

## 적용 상태

- 완료일: 2026-02-21
- 상태: 완료

## 변경 요약

| 항목 | 기존 (웹) | 변경 후 (모바일) |
|------|-----------|-----------------|
| Refresh Token 전달 | Set-Cookie 헤더 (HttpOnly) | 응답 바디 JSON |
| Reissue 요청 방식 | Cookie 자동 전송 | 요청 바디 `refreshToken` |
| Logout 요청 방식 | Cookie 자동 전송 | 요청 바디 `refreshToken` |
| CORS credentials | `allowCredentials(true)` | 제거 |
| CookieProvider | 사용 | 삭제 |
| CookieProperties | 사용 | 삭제 |

## 실제 반영 파일

### 수정

- `src/main/java/com/dochiri/kihan/presentation/auth/AuthController.java`
- `src/main/java/com/dochiri/kihan/presentation/auth/response/AuthResponse.java`
- `src/main/java/com/dochiri/kihan/infrastructure/config/SecurityConfig.java`
- `src/main/resources/application.yml`
- `src/test/java/com/dochiri/kihan/presentation/auth/AuthControllerTest.java`
- `frontend/kmp-android-sample/composeApp/src/commonMain/kotlin/com/dochiri/kihan/sample/ApiModels.kt`
- `frontend/kmp-android-sample/composeApp/src/commonMain/kotlin/com/dochiri/kihan/sample/KihanApiClient.kt`
- `frontend/kmp-android-sample/composeApp/src/commonMain/kotlin/com/dochiri/kihan/sample/App.kt`

### 추가

- `src/main/java/com/dochiri/kihan/presentation/auth/request/ReissueRequest.java`
- `src/main/java/com/dochiri/kihan/presentation/auth/request/LogoutRequest.java`

### 삭제

- `src/main/java/com/dochiri/kihan/infrastructure/security/cookie/CookieProvider.java`
- `src/main/java/com/dochiri/kihan/infrastructure/config/properties/CookieProperties.java`
- `src/test/java/com/dochiri/kihan/infrastructure/security/cookie/CookieProviderTest.java`

## API 계약 (적용 후)

### 로그인

`POST /api/auth/login`

요청:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

응답 `200`:

```json
{
  "userId": 1,
  "role": "USER",
  "accessToken": "....",
  "refreshToken": "...."
}
```

### 토큰 재발급

`POST /api/auth/reissue`

요청:

```json
{
  "refreshToken": "...."
}
```

응답 `200`:

```json
{
  "userId": 1,
  "role": "USER",
  "accessToken": "....",
  "refreshToken": "...."
}
```

### 로그아웃

`POST /api/auth/logout`

요청(선택):

```json
{
  "refreshToken": "...."
}
```

응답: `204 No Content`

## 검증 결과

- `./gradlew test --tests "com.dochiri.kihan.presentation.auth.AuthControllerTest"` 통과
- `./gradlew test` 전체 통과

## 보안 고려사항

- refresh token은 바디로 전달되므로 클라이언트 안전 저장 필수
- Android: EncryptedSharedPreferences 또는 Android Keystore
- iOS: Keychain Services
- 운영 환경은 HTTPS 필수
- 로그아웃 시 서버 토큰 폐기 + 클라이언트 저장 토큰 삭제를 함께 수행해야 완전 로그아웃
