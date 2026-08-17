# Routia Swagger API 명세

> 기준: 로컬 실행 서버의 `GET /v3/api-docs` 및 현재 Controller 구현  
> 대상: 인증, 온보딩, 유저 데이터, Web FCM 기기 관리, 알림 설정

## 공통 사항

- 인증이 필요한 API는 `Authorization: Bearer <Token>` Header를 사용한다.
- 인증 API의 성공 응답은 API별 DTO를 직접 반환하거나 Body가 없다.
- 그 외 API의 성공 응답은 아래 공통 형식을 사용한다.

```json
{
  "success": true,
  "data": {},
  "message": "요청이 성공했습니다."
}
```

- 애플리케이션 오류 응답은 아래 형식을 사용한다. 템플릿 예시의 `data/error` 중첩 형식은 현재 서버 Contract가 아니다.

```json
{
  "code": "ERROR_CODE",
  "message": "오류 메시지",
  "fieldErrors": []
}
```

- Spring Security에서 차단되는 미인증 요청은 현재 `401 Unauthorized`와 빈 Body를 반환한다.
- 날짜·시간은 ISO-8601 형식이다.

---

# 1. 인증 API

## 1.1 이메일 중복 확인

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 이메일 중복 확인 |
| BaseURL | `/api/v1/auth/email/check-duplicate` |
| Method | `GET` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | 인증 불필요 | - |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| email | `String` | 중복 확인할 이메일, 필수 | `user@example.com` |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

### Response

---

**요청 성공 — 200 OK**

```json
{
  "duplicated": false,
  "available": true
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| duplicated | `Boolean` | 이미 가입된 이메일인지 여부 | `false` |
| available | `Boolean` | 가입에 사용할 수 있는지 여부 | `true` |

**요청 실패**

```json
{
  "code": "INVALID_EMAIL_FORMAT",
  "message": "이메일 형식이 올바르지 않습니다.",
  "fieldErrors": [
    { "field": "email", "message": "이메일 형식이 올바르지 않습니다." }
  ]
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_EMAIL_FORMAT` | 이메일 형식 오류 |

---

## 1.2 이메일 인증번호 발송

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 이메일 인증번호 발송 |
| BaseURL | `/api/v1/auth/email/verification-code` |
| Method | `POST` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Content-Type | `String` | JSON 요청 | `application/json` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| email | `String` | 인증번호를 받을 이메일, 필수 | `user@example.com` |

```json
{
  "email": "user@example.com"
}
```

### Response

---

**요청 성공 — 200 OK**

```text
Response Body 없음
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | 인증번호 발급 및 메일 발송 성공 | - |

**요청 실패**

```json
{
  "code": "EMAIL_SEND_FAILED",
  "message": "이메일 발송에 실패했습니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_EMAIL_FORMAT` | 이메일 형식 오류 |
| 400 | `INVALID_REQUEST` | email 누락 또는 빈 값 |
| 500 | `EMAIL_SEND_FAILED` | SMTP 메일 발송 실패 |

---

## 1.3 이메일 인증번호 확인

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 이메일 인증번호 확인 |
| BaseURL | `/api/v1/auth/email/verify` |
| Method | `POST` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Content-Type | `String` | JSON 요청 | `application/json` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| email | `String` | 인증번호를 발급받은 이메일, 필수 | `user@example.com` |
| code | `String` | 메일로 수신한 6자리 숫자, 필수 | `123456` |

```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

### Response

---

**요청 성공 — 200 OK**

```text
Response Body 없음
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | 이메일 인증 성공 | - |

**요청 실패**

```json
{
  "code": "EMAIL_VERIFICATION_CODE_MISMATCH",
  "message": "인증번호가 올바르지 않습니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_EMAIL_FORMAT` | 이메일 형식 오류 |
| 400 | `INVALID_REQUEST` | 필수값 누락 또는 인증번호가 6자리 숫자가 아님 |
| 400 | `EMAIL_VERIFICATION_NOT_FOUND` | 발급된 인증정보 없음 |
| 400 | `EMAIL_VERIFICATION_CODE_MISMATCH` | 인증번호 불일치 |
| 400 | `EMAIL_VERIFICATION_EXPIRED` | 인증번호 만료 |

---

## 1.4 회원가입

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 회원가입 |
| BaseURL | `/api/v1/auth/signup` |
| Method | `POST` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Content-Type | `String` | JSON 요청 | `application/json` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| email | `String` | 인증 완료된 이메일, 필수 | `user@example.com` |
| password | `String` | 로그인 비밀번호, 필수 | `routia-password` |
| passwordConfirm | `String` | 비밀번호 확인, 필수 | `routia-password` |
| name | `String` | 사용자 이름, 1~50자 | `김루티` |

```json
{
  "email": "user@example.com",
  "password": "routia-password",
  "passwordConfirm": "routia-password",
  "name": "김루티"
}
```

### Response

---

**요청 성공 — 201 Created**

```text
Response Body 없음
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | ACTIVE 사용자 생성 성공 | - |

**요청 실패**

```json
{
  "code": "PASSWORD_CONFIRMATION_MISMATCH",
  "message": "비밀번호와 비밀번호 확인이 일치하지 않습니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_EMAIL_FORMAT` | 이메일 형식 오류 |
| 400 | `INVALID_REQUEST` | 필수값 누락 또는 이름 길이 오류 |
| 400 | `PASSWORD_CONFIRMATION_MISMATCH` | password와 passwordConfirm 불일치 |
| 400 | `EMAIL_NOT_VERIFIED` | 이메일 인증 미완료 |
| 400 | `EMAIL_VERIFICATION_EXPIRED` | 이메일 인증 만료 |
| 409 | `EMAIL_ALREADY_EXISTS` | 이미 가입된 이메일 |

---

## 1.5 로그인

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 로그인 |
| BaseURL | `/api/v1/auth/login` |
| Method | `POST` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Content-Type | `String` | JSON 요청 | `application/json` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| email | `String` | 가입한 이메일, 필수 | `user@example.com` |
| password | `String` | 가입 시 등록한 비밀번호, 필수 | `routia-password` |

```json
{
  "email": "user@example.com",
  "password": "routia-password"
}
```

### Response

---

**요청 성공 — 200 OK**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| accessToken | `String` | JWT Access Token | `eyJhbGciOiJIUzI1NiJ9...` |
| tokenType | `String` | Authorization scheme | `Bearer` |

**요청 실패**

```json
{
  "code": "INVALID_CREDENTIALS",
  "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_EMAIL_FORMAT` | 이메일 형식 오류 |
| 400 | `INVALID_REQUEST` | email 또는 password 누락 |
| 401 | `INVALID_CREDENTIALS` | 이메일 또는 비밀번호 불일치 |
| 403 | `ACCOUNT_BLOCKED` | 차단된 계정 |
| 403 | `ACCOUNT_WITHDRAWN` | 탈퇴한 계정 |

---

# 2. 온보딩 API

## 2.1 온보딩 0단계 저장

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 온보딩 0단계 저장 |
| BaseURL | `/api/v1/onboarding/step0` |
| Method | `POST` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |
| Content-Type | `String` | Multipart 요청 | `multipart/form-data` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| userName | `String` | 사용자 이름, 필수, 최대 50자 | `김루티` |
| profileImage | `File` | 선택 이미지, JPEG/PNG/WebP, 최대 5MB | `profile.png` |

```text
Content-Type: multipart/form-data
userName=김루티
profileImage=@profile.png
```

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "status": "IN_PROGRESS",
    "lastCompletedStep": 0,
    "step1CompletedAt": null,
    "step2CompletedAt": null,
    "step3CompletedAt": null,
    "completedAt": null
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data.status | `String` | 온보딩 상태 | `IN_PROGRESS` |
| data.lastCompletedStep | `Integer` | 완료된 마지막 정식 단계. Step0 완료 후에도 0 | `0` |
| data.step1CompletedAt | `Instant/null` | Step1 최초 완료 시각 | `null` |
| data.step2CompletedAt | `Instant/null` | Step2 최초 완료 시각 | `null` |
| data.step3CompletedAt | `Instant/null` | Step3 최초 완료 시각 | `null` |
| data.completedAt | `Instant/null` | 전체 완료 시각 | `null` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "INVALID_PROFILE_IMAGE",
  "message": "지원하지 않는 프로필 이미지 형식입니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | userName 누락 또는 길이 오류 |
| 400 | `INVALID_PROFILE_IMAGE` | 지원하지 않는 이미지 MIME/파일 시그니처 |
| 400 | `PROFILE_IMAGE_TOO_LARGE` | 이미지가 5MB를 초과함 |
| 401 | - | 인증 필요, 빈 Body |
| 403 | `ACCOUNT_BLOCKED` / `ACCOUNT_WITHDRAWN` | 사용 불가능한 계정 |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |

---

## 2.2 온보딩 1단계 저장

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 온보딩 1단계 저장 |
| BaseURL | `/api/v1/onboarding/step1` |
| Method | `POST` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |
| Content-Type | `String` | JSON 요청 | `application/json` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| height | `Decimal` | 키(cm), 양수, 소수점 1자리 | `165.5` |
| weight | `Decimal` | 몸무게(kg), 양수, 소수점 1자리 | `55.2` |
| gender | `String` | `FEMALE`, `MALE`, `UNSPECIFIED` | `FEMALE` |
| ageGroup | `String` | `TEENS`, `TWENTIES`, `THIRTIES`, `FORTIES`, `FIFTIES_PLUS` | `TWENTIES` |
| regionSido | `String` | 시·도, 필수, 최대 50자 | `서울특별시` |
| regionSigungu | `String` | 시·군·구, 필수, 최대 50자 | `강남구` |
| latitude | `Decimal` | 위도 -90~90, 소수점 최대 7자리 | `37.5172000` |
| longitude | `Decimal` | 경도 -180~180, 소수점 최대 7자리 | `127.0473000` |

```json
{
  "height": 165.5,
  "weight": 55.2,
  "gender": "FEMALE",
  "ageGroup": "TWENTIES",
  "regionSido": "서울특별시",
  "regionSigungu": "강남구",
  "latitude": 37.5172,
  "longitude": 127.0473
}
```

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "status": "IN_PROGRESS",
    "lastCompletedStep": 1,
    "step1CompletedAt": "2026-08-17T12:00:00Z",
    "step2CompletedAt": null,
    "step3CompletedAt": null,
    "completedAt": null
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data.status | `String` | 온보딩 상태 | `IN_PROGRESS` |
| data.lastCompletedStep | `Integer` | 마지막 완료 단계 | `1` |
| data.step1CompletedAt | `Instant` | Step1 최초 완료 시각 | `2026-08-17T12:00:00Z` |
| data.step2CompletedAt | `Instant/null` | Step2 최초 완료 시각 | `null` |
| data.step3CompletedAt | `Instant/null` | Step3 최초 완료 시각 | `null` |
| data.completedAt | `Instant/null` | 전체 완료 시각 | `null` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "ONBOARDING_STEP_ORDER_INVALID",
  "message": "이전 온보딩 단계를 먼저 완료해야 합니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | 필수값, 숫자 자릿수, enum 또는 좌표 범위 오류 |
| 400 | `INVALID_PROFILE_DATA` | 프로필·위치 조합이 유효하지 않음 |
| 401 | - | 인증 필요, 빈 Body |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |
| 409 | `ONBOARDING_STEP_ORDER_INVALID` | Step0 미완료 |

---

## 2.3 온보딩 2단계 저장

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 온보딩 2단계 저장 |
| BaseURL | `/api/v1/onboarding/step2` |
| Method | `POST` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |
| Content-Type | `String` | JSON 요청 | `application/json` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| skinType | `String` | `NORMAL`, `DRY`, `OILY`, `COMBINATION`, `SENSITIVE`, `DEHYDRATED_OILY` | `DRY` |
| skinConcerns | `String[]` | 피부 고민 코드, 0~3개, 최종 목록으로 교체 | `["ACNE", "PORE"]` |
| ownedTools | `String[]` | 보유 도구 코드, 0~4개, 최종 목록으로 교체 | `["FACE_FASCIA_TOOL"]` |
| bodyConcerns | `String[]` | 신체 고민 코드, 0~3개, 최종 목록으로 교체 | `["SWELLING"]` |
| bodyGoals | `String[]` | 신체 목표, 1~3개 | `["MAINTAIN", "BUILD_HABIT"]` |

```json
{
  "skinType": "DRY",
  "skinConcerns": ["ACNE", "PORE"],
  "ownedTools": ["FACE_FASCIA_TOOL"],
  "bodyConcerns": ["SWELLING"],
  "bodyGoals": ["MAINTAIN", "BUILD_HABIT"]
}
```

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "status": "IN_PROGRESS",
    "lastCompletedStep": 2,
    "step1CompletedAt": "2026-08-17T12:00:00Z",
    "step2CompletedAt": "2026-08-17T12:02:00Z",
    "step3CompletedAt": null,
    "completedAt": null
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data.status | `String` | 온보딩 상태 | `IN_PROGRESS` |
| data.lastCompletedStep | `Integer` | 마지막 완료 단계 | `2` |
| data.step1CompletedAt | `Instant` | Step1 최초 완료 시각 | `2026-08-17T12:00:00Z` |
| data.step2CompletedAt | `Instant` | Step2 최초 완료 시각 | `2026-08-17T12:02:00Z` |
| data.step3CompletedAt | `Instant/null` | Step3 최초 완료 시각 | `null` |
| data.completedAt | `Instant/null` | 전체 완료 시각 | `null` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "INVALID_SKIN_CONCERN",
  "message": "존재하지 않거나 사용할 수 없는 피부 고민입니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | 필수 목록 누락, 개수 제한 또는 enum 오류 |
| 400 | `INVALID_SKIN_CONCERN` | 유효하지 않은 피부 고민 코드 |
| 400 | `INVALID_BODY_CONCERN` | 유효하지 않은 신체 고민 코드 |
| 400 | `INVALID_BODY_GOAL` | 유효하지 않은 신체 목표 |
| 400 | `INVALID_OWNED_TOOL` | 유효하지 않은 보유 도구 코드 |
| 401 | - | 인증 필요, 빈 Body |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |
| 409 | `ONBOARDING_STEP_ORDER_INVALID` | Step1 미완료 |

---

## 2.4 온보딩 3단계 저장

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 온보딩 3단계 저장 |
| BaseURL | `/api/v1/onboarding/step3` |
| Method | `POST` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |
| Content-Type | `String` | JSON 요청 | `application/json` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| routineTimePreference | `String` | `MORNING`, `EVENING`, `ANY` | `MORNING` |
| routineDifficulty | `String` | `COMPLEX`, `SIMPLE`, `MINIMAL` | `SIMPLE` |

```json
{
  "routineTimePreference": "MORNING",
  "routineDifficulty": "SIMPLE"
}
```

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "status": "IN_PROGRESS",
    "lastCompletedStep": 3,
    "step1CompletedAt": "2026-08-17T12:00:00Z",
    "step2CompletedAt": "2026-08-17T12:02:00Z",
    "step3CompletedAt": "2026-08-17T12:04:00Z",
    "completedAt": null
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data.status | `String` | complete 호출 전 상태 | `IN_PROGRESS` |
| data.lastCompletedStep | `Integer` | 마지막 완료 단계 | `3` |
| data.step1CompletedAt | `Instant` | Step1 최초 완료 시각 | `2026-08-17T12:00:00Z` |
| data.step2CompletedAt | `Instant` | Step2 최초 완료 시각 | `2026-08-17T12:02:00Z` |
| data.step3CompletedAt | `Instant` | Step3 최초 완료 시각 | `2026-08-17T12:04:00Z` |
| data.completedAt | `Instant/null` | 전체 완료 시각 | `null` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "ONBOARDING_STEP_ORDER_INVALID",
  "message": "이전 온보딩 단계를 먼저 완료해야 합니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | 필수값 누락 또는 enum 오류 |
| 401 | - | 인증 필요, 빈 Body |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |
| 409 | `ONBOARDING_STEP_ORDER_INVALID` | Step2 미완료 |

> 알림 여부와 알림 시각은 Step3에서 받지 않는다. `/api/v1/users/{id}/notification-settings`에서 별도로 관리한다.

---

## 2.5 온보딩 진행 상태 조회

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 온보딩 진행 상태 조회 |
| BaseURL | `/api/v1/onboarding/progress` |
| Method | `GET` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "status": "IN_PROGRESS",
    "lastCompletedStep": 2,
    "step1CompletedAt": "2026-08-17T12:00:00Z",
    "step2CompletedAt": "2026-08-17T12:02:00Z",
    "step3CompletedAt": null,
    "completedAt": null
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data.status | `String` | `NOT_STARTED`, `IN_PROGRESS`, `GENERATING`, `COMPLETED`, `FAILED` | `IN_PROGRESS` |
| data.lastCompletedStep | `Integer` | 마지막 완료 단계, 0~3 | `2` |
| data.step1CompletedAt | `Instant/null` | Step1 최초 완료 시각 | `2026-08-17T12:00:00Z` |
| data.step2CompletedAt | `Instant/null` | Step2 최초 완료 시각 | `2026-08-17T12:02:00Z` |
| data.step3CompletedAt | `Instant/null` | Step3 최초 완료 시각 | `null` |
| data.completedAt | `Instant/null` | 전체 완료 시각 | `null` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```text
HTTP/1.1 401 Unauthorized
Response Body 없음
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 401 | - | 인증 필요, 빈 Body |

---

## 2.6 온보딩 완료 및 최초 AI 루틴 생성

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 온보딩 완료 및 최초 AI 루틴 생성 |
| BaseURL | `/api/v1/onboarding/complete` |
| Method | `POST` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | Step1~3에 저장된 사용자 데이터를 사용 | - |

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "status": "COMPLETED",
    "lastCompletedStep": 3,
    "step1CompletedAt": "2026-08-17T12:00:00Z",
    "step2CompletedAt": "2026-08-17T12:02:00Z",
    "step3CompletedAt": "2026-08-17T12:04:00Z",
    "completedAt": "2026-08-17T12:05:30Z",
    "routine": {
      "routineId": 21,
      "directionText": "가볍게 시작해 꾸준히 이어가는 아침 루틴입니다.",
      "homeComment": "오늘도 작은 습관부터 시작해 보세요.",
      "items": [
        {
          "timeSlot": "MORNING",
          "category": "SKIN",
          "title": "미온수 세안",
          "detail": "자극 없이 부드럽게 세안하세요.",
          "effectCode": "CLEAN",
          "expectedEffect": "피부 청결"
        }
      ]
    }
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data.status | `String` | 완료 상태 | `COMPLETED` |
| data.lastCompletedStep | `Integer` | 마지막 완료 단계 | `3` |
| data.step1CompletedAt | `Instant` | Step1 최초 완료 시각 | `2026-08-17T12:00:00Z` |
| data.step2CompletedAt | `Instant` | Step2 최초 완료 시각 | `2026-08-17T12:02:00Z` |
| data.step3CompletedAt | `Instant` | Step3 최초 완료 시각 | `2026-08-17T12:04:00Z` |
| data.completedAt | `Instant` | 온보딩 전체 완료 시각 | `2026-08-17T12:05:30Z` |
| data.routine.routineId | `Long` | 저장된 DailyRoutine ID | `21` |
| data.routine.directionText | `String` | AI 루틴 방향 설명 | `가볍게 시작하는 루틴` |
| data.routine.homeComment | `String` | 홈 화면용 코멘트 | `오늘도 시작해 보세요.` |
| data.routine.items | `Array` | 생성된 루틴 항목 목록 | `[...]` |
| data.routine.items[].timeSlot | `String` | 수행 시간대 | `MORNING` |
| data.routine.items[].category | `String` | 항목 카테고리 | `SKIN` |
| data.routine.items[].title | `String` | 루틴 제목 | `미온수 세안` |
| data.routine.items[].detail | `String` | 상세 수행 방법 | `부드럽게 세안하세요.` |
| data.routine.items[].effectCode | `String` | 기대 효과 코드 | `CLEAN` |
| data.routine.items[].expectedEffect | `String` | 기대 효과 설명 | `피부 청결` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "ROUTINE_GENERATION_FAILED",
  "message": "루틴 생성에 실패했습니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `ROUTINE_GENERATION_INPUT_INVALID` | 개인화 데이터가 루틴 생성에 부족함 |
| 401 | - | 인증 필요, 빈 Body |
| 404 | `USER_LOCATION_NOT_FOUND` | 사용자 위치 정보 없음 |
| 409 | `ONBOARDING_STEP_ORDER_INVALID` | Step1~3 미완료 또는 잘못된 상태 전환 |
| 502 | `ROUTINE_GENERATION_FAILED` | AI 루틴 생성 또는 READY 처리 실패 |
| 502 | `AI_RESPONSE_INVALID` | AI 응답 JSON 형식 오류 |
| 503 | `AI_PROVIDER_NOT_CONFIGURED` | AI Provider/API 설정 없음 |

> 이미 오늘의 루틴이 생성되어 있으면 AI를 다시 호출하지 않고 저장된 결과를 반환한다.

---

# 3. 유저 데이터 API

## 3.1 프로필 정보 조회

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 프로필 정보 조회 |
| BaseURL | `/api/v1/users/{id}/profile` |
| Method | `GET` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| id | `Long` | 조회할 사용자 ID. JWT 사용자와 동일해야 함 | `1` |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "userName": "김루티",
    "height": 165.5,
    "weight": 55.2,
    "gender": "FEMALE",
    "ageGroup": "TWENTIES",
    "profileImage": "profiles/1/profile.webp",
    "regionSido": "서울특별시",
    "regionSigungu": "강남구",
    "latitude": 37.5172,
    "longitude": 127.0473,
    "locationSource": "MANUAL",
    "locationUpdatedAt": "2026-08-17T12:00:00Z"
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data.userName | `String` | 사용자 이름 | `김루티` |
| data.height | `Decimal/null` | 키(cm) | `165.5` |
| data.weight | `Decimal/null` | 몸무게(kg) | `55.2` |
| data.gender | `String/null` | `FEMALE`, `MALE`, `UNSPECIFIED` | `FEMALE` |
| data.ageGroup | `String/null` | 연령대 | `TWENTIES` |
| data.profileImage | `String/null` | 저장된 프로필 이미지 객체 key | `profiles/1/profile.webp` |
| data.regionSido | `String/null` | 시·도 | `서울특별시` |
| data.regionSigungu | `String/null` | 시·군·구 | `강남구` |
| data.latitude | `Decimal/null` | 위도 | `37.5172` |
| data.longitude | `Decimal/null` | 경도 | `127.0473` |
| data.locationSource | `String/null` | `GPS`, `MANUAL` | `MANUAL` |
| data.locationUpdatedAt | `Instant/null` | 위치 변경 시각 | `2026-08-17T12:00:00Z` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "USER_DATA_ACCESS_DENIED",
  "message": "다른 사용자의 정보에 접근할 수 없습니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 401 | - | 인증 필요, 빈 Body |
| 403 | `USER_DATA_ACCESS_DENIED` | JWT 사용자와 PathVariable id 불일치 |
| 403 | `ACCOUNT_BLOCKED` / `ACCOUNT_WITHDRAWN` | 사용 불가능한 계정 |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |

---

## 3.2 프로필 정보 수정

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 프로필 정보 수정 |
| BaseURL | `/api/v1/users/{id}/profile` |
| Method | `PATCH` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |
| Content-Type | `String` | JSON 요청 | `application/json` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| id | `Long` | 수정할 사용자 ID. JWT 사용자와 동일해야 함 | `1` |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

> 모든 필드는 선택값이다. `null` 또는 생략한 필드는 기존 값을 유지한다.

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| height | `Decimal` | 키(cm), 양수, 소수점 1자리 | `166.0` |
| weight | `Decimal` | 몸무게(kg), 양수, 소수점 1자리 | `54.8` |
| gender | `String` | `FEMALE`, `MALE`, `UNSPECIFIED` | `FEMALE` |
| ageGroup | `String` | 연령대 enum | `TWENTIES` |
| regionSido | `String` | 시·도, 최대 50자 | `서울특별시` |
| regionSigungu | `String` | 시·군·구, 최대 50자 | `송파구` |
| latitude | `Decimal` | 위도 -90~90 | `37.5145` |
| longitude | `Decimal` | 경도 -180~180 | `127.1059` |
| locationSource | `String` | `GPS`, `MANUAL` | `MANUAL` |

```json
{
  "weight": 54.8,
  "regionSido": "서울특별시",
  "regionSigungu": "송파구",
  "latitude": 37.5145,
  "longitude": 127.1059,
  "locationSource": "MANUAL"
}
```

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "userName": "김루티",
    "height": 165.5,
    "weight": 54.8,
    "gender": "FEMALE",
    "ageGroup": "TWENTIES",
    "profileImage": "profiles/1/profile.webp",
    "regionSido": "서울특별시",
    "regionSigungu": "송파구",
    "latitude": 37.5145,
    "longitude": 127.1059,
    "locationSource": "MANUAL",
    "locationUpdatedAt": "2026-08-17T12:30:00Z"
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data | `ProfileResponse` | 변경 후 전체 프로필 | 위 JSON 참고 |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "INVALID_PROFILE_DATA",
  "message": "프로필 정보가 올바르지 않습니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | 숫자 자릿수, enum 또는 좌표 범위 오류 |
| 400 | `INVALID_PROFILE_DATA` | 위치 정보와 locationSource 조합 등이 유효하지 않음 |
| 401 | - | 인증 필요, 빈 Body |
| 403 | `USER_DATA_ACCESS_DENIED` | JWT 사용자와 id 불일치 |
| 403 | `ACCOUNT_BLOCKED` / `ACCOUNT_WITHDRAWN` | 사용 불가능한 계정 |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |

---

## 3.3 프로필 이미지 업로드

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 프로필 이미지 업로드 |
| BaseURL | `/api/v1/users/{id}/photo` |
| Method | `POST` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |
| Content-Type | `String` | Multipart 요청 | `multipart/form-data` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| id | `Long` | 사용자 ID. JWT 사용자와 동일해야 함 | `1` |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| file | `File` | JPEG/PNG/WebP 이미지, 필수, 최대 5MB | `profile.webp` |

```text
Content-Type: multipart/form-data
file=@profile.webp
```

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "userName": "김루티",
    "height": 165.5,
    "weight": 55.2,
    "gender": "FEMALE",
    "ageGroup": "TWENTIES",
    "profileImage": "profiles/1/550e8400.webp",
    "regionSido": "서울특별시",
    "regionSigungu": "강남구",
    "latitude": 37.5172,
    "longitude": 127.0473,
    "locationSource": "MANUAL",
    "locationUpdatedAt": "2026-08-17T12:00:00Z"
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data | `ProfileResponse` | 이미지 key가 반영된 전체 프로필 | 위 JSON 참고 |
| data.profileImage | `String` | 새로 저장된 이미지 객체 key | `profiles/1/550e8400.webp` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "PROFILE_IMAGE_TOO_LARGE",
  "message": "프로필 이미지는 5MB 이하여야 합니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | file 파트 누락 |
| 400 | `INVALID_PROFILE_IMAGE` | 지원하지 않는 MIME 또는 파일 시그니처 |
| 400 | `PROFILE_IMAGE_TOO_LARGE` | 파일 크기 5MB 초과 |
| 401 | - | 인증 필요, 빈 Body |
| 403 | `USER_DATA_ACCESS_DENIED` | JWT 사용자와 id 불일치 |
| 403 | `ACCOUNT_BLOCKED` / `ACCOUNT_WITHDRAWN` | 사용 불가능한 계정 |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |
| 500 | `PROFILE_IMAGE_STORAGE_FAILED` | 이미지 저장 실패 |

---

## 3.4 프로필 니즈 정보 조회

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 프로필 니즈 정보 조회 |
| BaseURL | `/api/v1/users/{id}/needs` |
| Method | `GET` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| id | `Long` | 조회할 사용자 ID. JWT 사용자와 동일해야 함 | `1` |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "bodyGoal": "MAINTAIN",
    "bodyGoals": ["MAINTAIN", "BUILD_HABIT"],
    "bodyConcerns": ["SWELLING"],
    "skinType": "DRY",
    "skinConcerns": ["ACNE", "PORE"],
    "ownedTools": ["FACE_FASCIA_TOOL"],
    "routineTimePreference": "MORNING",
    "routineDifficulty": "SIMPLE"
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data.bodyGoal | `String/null` | 대표 신체 목표, bodyGoals 첫 값과 동기화 | `MAINTAIN` |
| data.bodyGoals | `String[]` | 신체 목표 목록 | `["MAINTAIN", "BUILD_HABIT"]` |
| data.bodyConcerns | `String[]` | 신체 고민 코드 목록 | `["SWELLING"]` |
| data.skinType | `String/null` | 피부 타입 | `DRY` |
| data.skinConcerns | `String[]` | 피부 고민 코드 목록 | `["ACNE", "PORE"]` |
| data.ownedTools | `String[]` | 보유 도구 코드 목록 | `["FACE_FASCIA_TOOL"]` |
| data.routineTimePreference | `String/null` | `MORNING`, `EVENING`, `ANY` | `MORNING` |
| data.routineDifficulty | `String/null` | `COMPLEX`, `SIMPLE`, `MINIMAL` | `SIMPLE` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "USER_DATA_ACCESS_DENIED",
  "message": "다른 사용자의 정보에 접근할 수 없습니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 401 | - | 인증 필요, 빈 Body |
| 403 | `USER_DATA_ACCESS_DENIED` | JWT 사용자와 id 불일치 |
| 403 | `ACCOUNT_BLOCKED` / `ACCOUNT_WITHDRAWN` | 사용 불가능한 계정 |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |

---

## 3.5 프로필 니즈 정보 수정

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 프로필 니즈 정보 수정 |
| BaseURL | `/api/v1/users/{id}/needs` |
| Method | `PATCH` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |
| Content-Type | `String` | JSON 요청 | `application/json` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| id | `Long` | 수정할 사용자 ID. JWT 사용자와 동일해야 함 | `1` |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

> 모든 필드는 선택값이다. 목록을 전달하면 기존 목록과 merge하지 않고 전체 교체하며, 빈 배열로 모두 제거할 수 있다.

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| bodyGoal | `String` | 단일 대표 목표를 수정할 때 사용 | `FAT_LOSS` |
| bodyGoals | `String[]` | 목표 목록, 최대 3개. 전달 시 첫 값이 대표 목표 | `["FAT_LOSS", "BUILD_HABIT"]` |
| bodyConcerns | `String[]` | 신체 고민 최종 목록 | `["FATIGUE"]` |
| skinType | `String` | 피부 타입 enum | `SENSITIVE` |
| skinConcerns | `String[]` | 피부 고민 최종 목록 | `["ACNE"]` |
| ownedTools | `String[]` | 보유 도구 최종 목록, 최대 4개 | `["EXFOLIATING_PRODUCT"]` |
| routineTimePreference | `String` | `MORNING`, `EVENING`, `ANY` | `EVENING` |
| routineDifficulty | `String` | `COMPLEX`, `SIMPLE`, `MINIMAL` | `MINIMAL` |

```json
{
  "bodyGoals": ["FAT_LOSS", "BUILD_HABIT"],
  "bodyConcerns": ["FATIGUE"],
  "skinType": "SENSITIVE",
  "skinConcerns": ["ACNE"],
  "ownedTools": ["EXFOLIATING_PRODUCT"],
  "routineTimePreference": "EVENING",
  "routineDifficulty": "MINIMAL"
}
```

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "bodyGoal": "FAT_LOSS",
    "bodyGoals": ["FAT_LOSS", "BUILD_HABIT"],
    "bodyConcerns": ["FATIGUE"],
    "skinType": "SENSITIVE",
    "skinConcerns": ["ACNE"],
    "ownedTools": ["EXFOLIATING_PRODUCT"],
    "routineTimePreference": "EVENING",
    "routineDifficulty": "MINIMAL"
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data | `NeedsResponse` | 변경 후 전체 니즈 정보 | 위 JSON 참고 |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "INVALID_OWNED_TOOL",
  "message": "존재하지 않거나 사용할 수 없는 보유 도구입니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | 목록 개수, 원소 길이 또는 enum 오류 |
| 400 | `INVALID_BODY_CONCERN` | 유효하지 않은 신체 고민 코드 |
| 400 | `INVALID_SKIN_CONCERN` | 유효하지 않은 피부 고민 코드 |
| 400 | `INVALID_BODY_GOAL` | 유효하지 않은 신체 목표 |
| 400 | `INVALID_OWNED_TOOL` | 유효하지 않은 보유 도구 코드 |
| 401 | - | 인증 필요, 빈 Body |
| 403 | `USER_DATA_ACCESS_DENIED` | JWT 사용자와 id 불일치 |
| 403 | `ACCOUNT_BLOCKED` / `ACCOUNT_WITHDRAWN` | 사용 불가능한 계정 |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |


---

# 4. Web FCM 기기 관리 API

> Swagger의 `알림` 태그는 알림 목록 조회가 아니라 Web Push를 수신할 브라우저 기기 등록·비활성화 API다. 현재 Request Contract는 `fcmToken`이 아니라 Firebase Installation ID인 `installationId`를 사용한다.

## 4.1 Web Push 기기 등록

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | Web Push 기기 등록 |
| BaseURL | `/api/v1/users/{id}/push-devices` |
| Method | `POST` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |
| Content-Type | `String` | JSON 요청 | `application/json` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| id | `Long` | 기기를 등록할 사용자 ID. JWT 사용자와 동일해야 함 | `1` |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| installationId | `String` | Frontend가 Firebase에서 획득한 FID, 필수, 최대 255자 | `cV8...firebase-installation-id` |
| platform | `String` | 현재 `WEB`만 허용 | `WEB` |

```json
{
  "installationId": "cV8...firebase-installation-id",
  "platform": "WEB"
}
```

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "id": 10,
    "platform": "WEB",
    "active": true,
    "lastSeenAt": "2026-08-17T12:30:00Z"
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data.id | `Long` | Push 기기 row ID. 비활성화 API에 사용 | `10` |
| data.platform | `String` | 플랫폼 | `WEB` |
| data.active | `Boolean` | 발송 대상 활성화 여부 | `true` |
| data.lastSeenAt | `Instant` | 마지막 등록·재활성화 시각 | `2026-08-17T12:30:00Z` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "INVALID_PUSH_DEVICE",
  "message": "푸시 기기 정보가 올바르지 않습니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | 필수값 누락 또는 installationId 길이 오류 |
| 400 | `INVALID_PUSH_DEVICE` | 빈 FID, 255자 초과 또는 WEB 이외 플랫폼 |
| 401 | - | 인증 필요, 빈 Body |
| 403 | `USER_DATA_ACCESS_DENIED` | JWT 사용자와 id 불일치 |
| 403 | `ACCOUNT_BLOCKED` / `ACCOUNT_WITHDRAWN` | 사용 불가능한 계정 |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |

> 같은 FID를 다시 등록하면 기존 row가 재활성화된다. 다른 사용자가 같은 FID를 등록하면 현재 사용자에게 소유권이 이전된다.

---

## 4.2 Web Push 기기 비활성화

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | Web Push 기기 비활성화 |
| BaseURL | `/api/v1/users/{id}/push-devices/{deviceId}` |
| Method | `DELETE` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| id | `Long` | 사용자 ID. JWT 사용자와 동일해야 함 | `1` |
| deviceId | `Long` | 기기 등록 응답에서 받은 기기 ID | `10` |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "id": 10,
    "platform": "WEB",
    "active": false,
    "lastSeenAt": "2026-08-17T12:30:00Z"
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data.id | `Long` | 기기 row ID | `10` |
| data.platform | `String` | 플랫폼 | `WEB` |
| data.active | `Boolean` | 비활성화 결과 | `false` |
| data.lastSeenAt | `Instant` | 마지막 활성 등록 시각 | `2026-08-17T12:30:00Z` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "PUSH_DEVICE_NOT_FOUND",
  "message": "푸시 기기를 찾을 수 없습니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 401 | - | 인증 필요, 빈 Body |
| 403 | `USER_DATA_ACCESS_DENIED` | JWT 사용자와 id 불일치 |
| 403 | `ACCOUNT_BLOCKED` / `ACCOUNT_WITHDRAWN` | 사용 불가능한 계정 |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |
| 404 | `PUSH_DEVICE_NOT_FOUND` | 해당 사용자의 deviceId가 존재하지 않음 |

> DB row를 삭제하지 않고 `is_active=false`로 변경하여 이후 발송 대상에서 제외한다.

---

# 5. 알림 설정 API

> 사용자 전체의 알림 ON/OFF 및 알림 희망 시각을 관리한다. 활성 Push 기기가 있어도 이 설정이 OFF이면 알림을 발송하지 않는다.

## 5.1 알림 설정 조회

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 알림 설정 조회 |
| BaseURL | `/api/v1/users/{id}/notification-settings` |
| Method | `GET` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| id | `Long` | 사용자 ID. JWT 사용자와 동일해야 함 | `1` |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "notificationEnabled": true,
    "notificationTime": "08:00"
  },
  "message": "요청이 성공했습니다."
}
```

설정이 아직 없으면 다음과 같이 반환한다.

```json
{
  "success": true,
  "data": {
    "notificationEnabled": false,
    "notificationTime": null
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data.notificationEnabled | `Boolean` | Push 알림 활성화 여부 | `true` |
| data.notificationTime | `String/null` | `HH:mm` 형식 알림 시각 | `08:00` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "USER_DATA_ACCESS_DENIED",
  "message": "다른 사용자의 정보에 접근할 수 없습니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 401 | - | 인증 필요, 빈 Body |
| 403 | `USER_DATA_ACCESS_DENIED` | JWT 사용자와 id 불일치 |
| 403 | `ACCOUNT_BLOCKED` / `ACCOUNT_WITHDRAWN` | 사용 불가능한 계정 |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |

---

## 5.2 알림 설정 수정

### API 기본 정보

| 항목 | 내용 |
| --- | --- |
| API 명 | 알림 설정 수정 |
| BaseURL | `/api/v1/users/{id}/notification-settings` |
| Method | `PATCH` |

### Request

---

**Header**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| Authorization | `String` | JWT | `Bearer <Token>` |
| Content-Type | `String` | JSON 요청 | `application/json` |

**PathVariable**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| id | `Long` | 사용자 ID. JWT 사용자와 동일해야 함 | `1` |

**Parameter**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| 없음 | - | - | - |

**Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| notificationEnabled | `Boolean` | 알림 ON/OFF, 필수 | `true` |
| notificationTime | `String/null` | `HH:mm` 형식. 처음 ON으로 설정할 때 필수 | `08:00` |

알림 ON:

```json
{
  "notificationEnabled": true,
  "notificationTime": "08:00"
}
```

알림 OFF:

```json
{
  "notificationEnabled": false,
  "notificationTime": null
}
```

### Response

---

**요청 성공 — 200 OK**

```json
{
  "success": true,
  "data": {
    "notificationEnabled": true,
    "notificationTime": "08:00"
  },
  "message": "요청이 성공했습니다."
}
```

**Response Body**

| Key | Type | Description | Example |
| --- | --- | --- | --- |
| success | `Boolean` | 요청 성공 여부 | `true` |
| data.notificationEnabled | `Boolean` | 변경 후 알림 활성화 여부 | `true` |
| data.notificationTime | `String/null` | 저장된 알림 시각 | `08:00` |
| message | `String` | 응답 메시지 | `요청이 성공했습니다.` |

**요청 실패**

```json
{
  "code": "INVALID_NOTIFICATION_SETTINGS",
  "message": "알림 설정이 올바르지 않습니다.",
  "fieldErrors": []
}
```

**발생할 수 있는 오류 코드**

| HTTP Status | Code | Description |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | notificationEnabled 누락 또는 `HH:mm` 형식 오류 |
| 400 | `INVALID_NOTIFICATION_SETTINGS` | 저장된 시각 없이 알림을 ON으로 요청함 |
| 401 | - | 인증 필요, 빈 Body |
| 403 | `USER_DATA_ACCESS_DENIED` | JWT 사용자와 id 불일치 |
| 403 | `ACCOUNT_BLOCKED` / `ACCOUNT_WITHDRAWN` | 사용 불가능한 계정 |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |

> 알림을 OFF로 변경할 때 `notificationTime`을 생략하거나 null로 전달하면 기존 알림 시각은 보존된다. 알림 OFF 상태에서도 일일 루틴 생성은 계속된다.

---

# 6. 코드 값 참고

## 신체 고민 코드

| Code | 의미 |
| --- | --- |
| `SWELLING` | 붓기 |
| `FATIGUE` | 피로감 |
| `BODY_SHAPE_CHANGE` | 체형 변화 |
| `CIRCULATION` | 혈액순환 |
| `WEIGHT_LOSS` | 체중 감소 |

## 피부 고민 코드

| Code | 의미 |
| --- | --- |
| `ACNE` | 여드름 |
| `PORE` | 모공 |
| `ELASTICITY` | 탄력 |
| `WRINKLE` | 주름 |
| `PIGMENTATION` | 색소 |
| `SEBUM` | 피지 |

## 신체 목표 코드

| Code | 의미 |
| --- | --- |
| `MUSCLE_GAIN` | 근육 증가 |
| `MAINTAIN` | 현재 상태 유지 |
| `FAT_LOSS` | 체지방 감소 |
| `BUILD_HABIT` | 생활 습관 형성 |
| `REGULAR_LIFE` | 규칙적인 생활 |

## 보유 도구 코드

| Code | 의미 |
| --- | --- |
| `SKINCARE_ABSORPTION_DEVICE` | 스킨케어 흡수 기기 |
| `BODY_FASCIA_TOOL` | 바디 괄사 |
| `FACE_FASCIA_TOOL` | 페이스 괄사 |
| `EXFOLIATING_PRODUCT` | 각질 제거 제품 |

## 주요 Enum

| 분류 | 허용 값 |
| --- | --- |
| Gender | `FEMALE`, `MALE`, `UNSPECIFIED` |
| AgeGroup | `TEENS`, `TWENTIES`, `THIRTIES`, `FORTIES`, `FIFTIES_PLUS` |
| SkinType | `NORMAL`, `DRY`, `OILY`, `COMBINATION`, `SENSITIVE`, `DEHYDRATED_OILY` |
| RoutineTimePreference | `MORNING`, `EVENING`, `ANY` |
| RoutineDifficulty | `COMPLEX`, `SIMPLE`, `MINIMAL` |
| LocationSource | `GPS`, `MANUAL` |
| OnboardingStatus | `NOT_STARTED`, `IN_PROGRESS`, `GENERATING`, `COMPLETED`, `FAILED` |
| 500 | `PROFILE_IMAGE_STORAGE_FAILED` | 이미지 저장 실패 |

---

