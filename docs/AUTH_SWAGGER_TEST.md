# Auth Swagger 실연동 테스트

## 1. 실행 전 환경변수

IntelliJ Run Configuration의 Environment variables에 아래 값을 설정한다.

| 변수 | 설명 | 예시 |
| --- | --- | --- |
| `DB_URL` | Auth DDL이 적용된 MySQL JDBC URL | `jdbc:mysql://host:3306/routia?serverTimezone=UTC` |
| `DB_USERNAME` | MySQL 사용자 | `routia_app` |
| `DB_PASSWORD` | MySQL 비밀번호 | 실제 Secret |
| `JPA_DDL_AUTO` | 운영 스키마 자동 변경 방지 | `none` |
| `MAIL_HOST` | SMTP 서버 | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP 포트 | `587` |
| `MAIL_USERNAME` | SMTP 계정 | 발신 계정 이메일 |
| `MAIL_PASSWORD` | SMTP 비밀번호 | 앱 비밀번호 등 실제 Secret |
| `MAIL_FROM` | 메일 발신자 | `no-reply@routia.com` |
| `MAIL_SMTP_AUTH` | SMTP 인증 사용 | `true` |
| `MAIL_SMTP_STARTTLS` | STARTTLS 사용 | `true` |
| `JWT_SECRET` | HS256 서명 Secret | 32바이트 이상의 랜덤 문자열 권장 |
| `JWT_ACCESS_EXPIRATION` | Access Token 유효시간(초) | `3600` |

Secret은 `.env.example`이나 Git 추적 파일에 입력하지 않는다.

## 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

실행 후 다음 주소를 연다.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 3. 전체 인증 흐름

각 요청은 Swagger UI의 `Try it out`으로 실행한다. 모든 단계에서 같은 이메일을 사용한다.

### 3.1 이메일 중복 확인

`GET /api/v1/auth/email/check-duplicate?email=user@example.com`

```json
{
  "duplicated": false,
  "available": true
}
```

### 3.2 인증번호 발급

`POST /api/v1/auth/email/verification-code`

```json
{
  "email": "user@example.com"
}
```

성공 시 `200 OK`이며 실제 메일함에서 `[Routia] 이메일 인증번호 안내` 메일과 6자리 번호를 확인한다.

DB에서는 평문 코드가 아닌 BCrypt hash와 5분 유효시간이 저장되어야 한다.

```sql
SELECT id, email, purpose, code_hash, expires_at, verified_at, consumed_at, created_at
FROM email_verifications
WHERE email = 'user@example.com'
ORDER BY created_at DESC, id DESC
LIMIT 1;
```

### 3.3 인증번호 검증

`POST /api/v1/auth/email/verify`

```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

메일에서 받은 실제 번호로 바꿔 실행한다. 성공 후 최신 row의 `verified_at`이 채워져야 한다.

### 3.4 회원가입

`POST /api/v1/auth/signup`

```json
{
  "email": "user@example.com",
  "password": "routia-password",
  "name": "김루티"
}
```

성공 시 `201 Created`이며 `users` row와 인증정보의 `consumed_at`을 확인한다.

```sql
SELECT id, email, password_hash, name, account_status, email_verified_at, created_at
FROM users
WHERE email = 'user@example.com';
```

`password_hash`에는 평문 비밀번호가 존재하면 안 된다.

### 3.5 로그인

`POST /api/v1/auth/login`

```json
{
  "email": "user@example.com",
  "password": "routia-password"
}
```

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer"
}
```

Swagger UI 우측 상단 `Authorize`를 누르고 `accessToken` 값만 입력한다. Swagger가 요청 시 `Authorization: Bearer eyJ...` 형식으로 전송한다.

## 4. 필수 실패 테스트

| 상황 | 기대 결과 |
| --- | --- |
| 잘못된 이메일 형식 | `400 INVALID_EMAIL_FORMAT` |
| 존재하지 않는 인증정보 | `400 EMAIL_VERIFICATION_NOT_FOUND` |
| 잘못된 인증번호 | `400 EMAIL_VERIFICATION_CODE_MISMATCH` |
| 5분이 지난 인증번호 | `400 EMAIL_VERIFICATION_EXPIRED` |
| 인증하지 않고 회원가입 | `400 EMAIL_NOT_VERIFIED` |
| 같은 이메일 재가입 | `409 EMAIL_ALREADY_EXISTS` |
| 잘못된 로그인 정보 | `401 INVALID_CREDENTIALS` |
| SMTP 연결 실패 | `500 EMAIL_SEND_FAILED`, verification은 consumed 처리 |

## 5. 테스트 완료 기준

- Swagger UI에서 Auth 5개 API가 보이고 요청 예시가 자동 입력된다.
- 실제 RDS의 두 Auth 테이블에 데이터가 저장·갱신된다.
- 실제 메일함에 HTML 인증 메일이 도착한다.
- 인증번호 평문과 비밀번호 평문이 DB에 남지 않는다.
- 로그인 응답 JWT의 subject가 생성된 `users.id`와 일치한다.
