# Routia Backend 전체 Swagger 테스트 가이드

## 1. 실제 OpenAI 연동 확인 결과

Onboarding V2 입력을 반영한 OpenAI Responses API 라이브 테스트를 실행했다.

```text
CASE_A_MORNING_HIGH_UV  : MORNING 5 / AFTERNOON 1 / EVENING 1 / BEDTIME 1
CASE_B_EVENING          : MORNING 2 / AFTERNOON 1 / EVENING 3 / BEDTIME 2
CASE_C_LOW_PERFORMANCE  : MORNING 5 / AFTERNOON 2 / BEDTIME 1
CASE_D_NEW_USER_ANY     : MORNING 3 / AFTERNOON 2 / EVENING 2 / BEDTIME 1

BUILD SUCCESSFUL
```

아래 Swagger 순서는 Auth부터 실제 `/onboarding/complete` 호출까지 전체 애플리케이션 흐름을 검증한다.

## 2. 실행 전 필수 설정

### DB 반영

애플리케이션 실행 전에 다음 SQL을 RDS에 반영한다.

```text
docs/database/onboarding-v2.sql
```

추가·변경 사항:

- `body_goals`
- `user_body_goals`
- `owned_tools`
- `user_owned_tools`
- `routine_schedules.notification_time` nullable 변경
- 신규 Master Data

### `.env`

```properties
AI_PROVIDER=openai
OPENAI_API_KEY=실제_API_KEY
AI_MODEL=gpt-5.6-luna
AI_PROMPT_VERSION=routine-v2-onboarding-v2
AI_PROMPT_RESOURCE=classpath:prompts/routine-v2.txt
AI_REASONING_EFFORT=low
AI_LOG_RESPONSE=true
```

API Key는 Swagger Request에 넣지 않는다. 서버 환경변수로만 사용한다.

### 실행 및 Swagger 접속

```bash
./gradlew bootRun
```

```text
http://localhost:8080/swagger-ui.html
```

## 3. Swagger 인증 방법

1. 로그인 응답의 `accessToken`을 복사한다.
2. Swagger 우측 상단 `Authorize`를 누른다.
3. `Bearer ` 접두사 없이 토큰 값만 입력한다.
4. 이후 보호 API를 순서대로 호출한다.

`/users/{id}`의 `id`는 JWT payload의 `sub` 값이다. 현재 로그인 응답에는 `userId`가 없으므로 JWT를 decode하거나 DB에서 이메일로 조회한다.

## 4. Auth

### 4.1 이메일 중복 확인

```http
GET /api/v1/auth/email/check-duplicate?email=routia-swagger@example.com
```

```json
{
  "duplicated": false,
  "available": true
}
```

### 4.2 인증번호 발송

```http
POST /api/v1/auth/email/verification-code
Content-Type: application/json
```

```json
{
  "email": "routia-swagger@example.com"
}
```

성공: `200 OK`, 응답 본문 없음.

### 4.3 인증번호 확인

```http
POST /api/v1/auth/email/verify
Content-Type: application/json
```

```json
{
  "email": "routia-swagger@example.com",
  "code": "메일로 받은 6자리 숫자"
}
```

성공: `200 OK`, 응답 본문 없음.

### 4.4 회원가입

현재 Auth Contract상 `name`은 여전히 필수이며 Step0에서 최종 이름으로 갱신한다.

```http
POST /api/v1/auth/signup
Content-Type: application/json
```

```json
{
  "email": "routia-swagger@example.com",
  "password": "routia-password",
  "name": "가입이름"
}
```

성공: `201 Created`, 응답 본문 없음.

### 4.5 로그인

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "routia-swagger@example.com",
  "password": "routia-password"
}
```

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer"
}
```

## 5. Onboarding V2

모든 API는 Swagger `Authorize` 이후 호출한다. 순서를 바꾸면 `409 ONBOARDING_STEP_ORDER_INVALID`가 정상이다.

### 5.1 Step0 — 이름과 프로필 이미지

```http
POST /api/v1/onboarding/step0
Content-Type: multipart/form-data
```

Swagger 입력:

```text
userName: 루티아
profileImage: JPEG/PNG/WebP 파일 선택 (선택, 최대 5MB)
```

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

### 5.2 Step1 — 신체 정보와 거주지

```http
POST /api/v1/onboarding/step1
Content-Type: application/json
```

```json
{
  "height": 165.5,
  "weight": 55.2,
  "gender": "FEMALE",
  "ageGroup": "TWENTIES",
  "regionSido": "서울특별시",
  "regionSigungu": "중구",
  "latitude": 37.5665,
  "longitude": 126.978
}
```

```json
{
  "success": true,
  "data": {
    "status": "IN_PROGRESS",
    "lastCompletedStep": 1,
    "step1CompletedAt": "2026-08-17T09:00:00Z",
    "step2CompletedAt": null,
    "step3CompletedAt": null,
    "completedAt": null
  },
  "message": "요청이 성공했습니다."
}
```

위도·경도는 `/complete`에서 실제 Open-Meteo 날씨 조회에 사용하므로 반드시 실제 범위의 값을 넣는다.

### 5.3 Step2 — 피부/신체 고민, 다중 목표, 보유 도구

```http
POST /api/v1/onboarding/step2
Content-Type: application/json
```

```json
{
  "skinType": "COMBINATION",
  "skinConcerns": ["ELASTICITY", "PORE"],
  "ownedTools": ["FACE_FASCIA_TOOL"],
  "bodyConcerns": ["FATIGUE", "SWELLING"],
  "bodyGoals": ["BUILD_HABIT", "REGULAR_LIFE"]
}
```

```json
{
  "success": true,
  "data": {
    "status": "IN_PROGRESS",
    "lastCompletedStep": 2,
    "step1CompletedAt": "<기존 시각>",
    "step2CompletedAt": "<현재 시각>",
    "step3CompletedAt": null,
    "completedAt": null
  },
  "message": "요청이 성공했습니다."
}
```

허용값:

```text
skinType:
NORMAL, DRY, OILY, COMBINATION, SENSITIVE, DEHYDRATED_OILY

skinConcerns (최대 3):
ACNE, PORE, ELASTICITY, WRINKLE, PIGMENTATION, SEBUM

bodyConcerns (최대 3):
SWELLING, FATIGUE, BODY_SHAPE_CHANGE, CIRCULATION, WEIGHT_LOSS

bodyGoals (1~3):
MUSCLE_GAIN, MAINTAIN, FAT_LOSS, BUILD_HABIT, REGULAR_LIFE

ownedTools (최대 4):
SKINCARE_ABSORPTION_DEVICE, BODY_FASCIA_TOOL,
FACE_FASCIA_TOOL, EXFOLIATING_PRODUCT
```

### 5.4 Step3 — 루틴 선호

```http
POST /api/v1/onboarding/step3
Content-Type: application/json
```

```json
{
  "routineTimePreference": "MORNING",
  "routineDifficulty": "SIMPLE"
}
```

```json
{
  "success": true,
  "data": {
    "status": "IN_PROGRESS",
    "lastCompletedStep": 3,
    "step1CompletedAt": "<기존 시각>",
    "step2CompletedAt": "<기존 시각>",
    "step3CompletedAt": "<현재 시각>",
    "completedAt": null
  },
  "message": "요청이 성공했습니다."
}
```

허용값:

```text
routineTimePreference: MORNING, EVENING, ANY
routineDifficulty: MINIMAL(4개), SIMPLE(8개), COMPLEX(12개)
```

### 5.5 진행 상태 조회

```http
GET /api/v1/onboarding/progress
```

```json
{
  "success": true,
  "data": {
    "status": "IN_PROGRESS",
    "lastCompletedStep": 3,
    "step1CompletedAt": "<저장 시각>",
    "step2CompletedAt": "<저장 시각>",
    "step3CompletedAt": "<저장 시각>",
    "completedAt": null
  },
  "message": "요청이 성공했습니다."
}
```

### 5.6 Complete — 실제 OpenAI 루틴 생성

```http
POST /api/v1/onboarding/complete
```

Request body 없음.

SIMPLE이면 정상적으로 `items` 8개가 반환되어야 한다. AI 문장은 매 호출 환경에 따라 달라질 수 있다.

```json
{
  "success": true,
  "data": {
    "status": "COMPLETED",
    "lastCompletedStep": 3,
    "step1CompletedAt": "<저장 시각>",
    "step2CompletedAt": "<저장 시각>",
    "step3CompletedAt": "<저장 시각>",
    "completedAt": "<완료 시각>",
    "routine": {
      "routineId": 101,
      "directionText": "피로와 붓기를 고려해 아침에 실행하기 쉬운 관리 행동을 우선해 보세요.",
      "homeComment": "아침 선호와 현재 날씨, 피부·신체 고민을 함께 반영했습니다.",
      "items": [
        {
          "timeSlot": "MORNING",
          "category": "LIFESTYLE",
          "title": "기상 후 물 한 컵 마시기",
          "detail": "일어난 뒤 물 한 컵을 천천히 마셔 하루를 시작하세요.",
          "effectCode": "HYDRATION",
          "expectedEffect": "수분 보충과 아침 습관 형성에 도움을 줄 수 있습니다."
        }
      ]
    }
  },
  "message": "요청이 성공했습니다."
}
```

확인 항목:

- `status=COMPLETED`
- `routine.routineId` 존재
- SIMPLE이면 `routine.items.length=8`
- MORNING이면 약 5개가 MORNING에 배치
- 저장된 `bodyGoals`, `ownedTools`, 고민과 날씨가 문장·항목에 반영
- 응답 항목이 `daily_routines`, `routine_items`에 저장
- 최초 Schedule은 알림 OFF, 다음 생성 시각은 06:00 KST

IntelliJ Console에는 다음 로그가 출력된다.

```text
OpenAI routine response received. model=gpt-5.6-luna
promptVersion=routine-v2-onboarding-v2
routineDate=...
response={...실제 생성 결과...}
```

같은 날짜에 `/complete`를 다시 호출하면 같은 `routineId`와 저장 결과를 반환하며 OpenAI를 다시 호출하지 않는다.

## 6. Notification Settings

`{id}`는 JWT `sub`의 사용자 ID로 바꾼다.

### 6.1 조회

```http
GET /api/v1/users/{id}/notification-settings
```

Complete 직후 기본값:

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

### 6.2 ON

```http
PATCH /api/v1/users/{id}/notification-settings
Content-Type: application/json
```

```json
{
  "notificationEnabled": true,
  "notificationTime": "08:00"
}
```

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

다음 루틴은 07:50 KST에 생성되고 알림 예정 시각은 08:00 KST가 된다.

### 6.3 OFF

```json
{
  "notificationEnabled": false
}
```

```json
{
  "success": true,
  "data": {
    "notificationEnabled": false,
    "notificationTime": "08:00"
  },
  "message": "요청이 성공했습니다."
}
```

OFF여도 일일 루틴 생성은 계속되며 이전 알림 시각은 보존된다.

## 7. User Data

### 7.1 프로필 조회

```http
GET /api/v1/users/{id}/profile
```

```json
{
  "success": true,
  "data": {
    "userName": "루티아",
    "height": 165.5,
    "weight": 55.2,
    "gender": "FEMALE",
    "ageGroup": "TWENTIES",
    "profileImage": "profiles/1/example.webp",
    "regionSido": "서울특별시",
    "regionSigungu": "중구",
    "latitude": 37.5665,
    "longitude": 126.978,
    "locationSource": "MANUAL",
    "locationUpdatedAt": "<저장 시각>"
  },
  "message": "요청이 성공했습니다."
}
```

### 7.2 프로필 수정

```http
PATCH /api/v1/users/{id}/profile
Content-Type: application/json
```

```json
{
  "weight": 54.8,
  "regionSido": "서울특별시",
  "regionSigungu": "강남구",
  "latitude": 37.5172,
  "longitude": 127.0473,
  "locationSource": "MANUAL"
}
```

응답은 7.1과 같은 전체 Profile 구조이며 수정하지 않은 필드는 유지된다.

### 7.3 프로필 이미지 업로드

```http
POST /api/v1/users/{id}/photo
Content-Type: multipart/form-data
```

```text
file: JPEG/PNG/WebP 파일
```

응답은 변경된 `profileImage`를 포함하는 전체 Profile 구조다.

### 7.4 니즈 조회

```http
GET /api/v1/users/{id}/needs
```

```json
{
  "success": true,
  "data": {
    "bodyGoal": "BUILD_HABIT",
    "bodyGoals": ["BUILD_HABIT", "REGULAR_LIFE"],
    "bodyConcerns": ["SWELLING", "FATIGUE"],
    "skinType": "COMBINATION",
    "skinConcerns": ["PORE", "ELASTICITY"],
    "ownedTools": ["FACE_FASCIA_TOOL"],
    "routineTimePreference": "MORNING",
    "routineDifficulty": "SIMPLE"
  },
  "message": "요청이 성공했습니다."
}
```

`bodyGoal`은 하위 호환용 첫 번째 목표이며 신규 구현에서는 `bodyGoals`를 사용한다.

### 7.5 니즈 수정

```http
PATCH /api/v1/users/{id}/needs
Content-Type: application/json
```

```json
{
  "bodyGoals": ["FAT_LOSS", "BUILD_HABIT"],
  "bodyConcerns": ["FATIGUE"],
  "skinType": "NORMAL",
  "skinConcerns": [],
  "ownedTools": ["EXFOLIATING_PRODUCT"],
  "routineTimePreference": "EVENING",
  "routineDifficulty": "MINIMAL"
}
```

응답은 7.4와 같은 전체 Needs 구조다. 배열은 merge가 아니라 최종값으로 전체 교체된다.

## 8. 생성 이후 사용자 화면 API

### 홈

```http
GET /api/v1/home
```

```json
{
  "success": true,
  "data": {
    "userName": "루티아",
    "date": "2026-08-17",
    "directionText": "오늘의 AI 관리 방향",
    "homeComment": "오늘의 AI 홈 코멘트",
    "progressPercent": 0,
    "completedCount": 0,
    "totalCount": 8,
    "todayTasks": [
      {"itemId": 1001, "title": "기상 후 물 한 컵 마시기", "completed": false}
    ]
  },
  "message": "요청이 성공했습니다."
}
```

### 날씨/자외선

```http
GET /api/v1/weather/today
```

```json
{
  "success": true,
  "data": {
    "regionSido": "서울특별시",
    "regionSigungu": "중구",
    "temperature": 27.0,
    "feelsLike": 28.0,
    "weatherDescription": "구름 조금",
    "temperatureTip": "<동적 안내>",
    "uvIndex": 5.2,
    "uvLevel": "<자외선 단계>",
    "uvTip": "<동적 안내>"
  },
  "message": "요청이 성공했습니다."
}
```

### 오늘 루틴 전체 조회

```http
GET /api/v1/routines/today
```

```json
{
  "success": true,
  "data": {
    "routineId": 101,
    "date": "2026-08-17",
    "directionText": "오늘의 AI 관리 방향",
    "homeComment": "오늘의 AI 홈 코멘트",
    "completedCount": 0,
    "totalCount": 8,
    "items": [
      {
        "itemId": 1001,
        "timeSlot": "MORNING",
        "category": "LIFESTYLE",
        "title": "기상 후 물 한 컵 마시기",
        "detail": "일어난 뒤 물 한 컵을 천천히 마시세요.",
        "effectCode": "HYDRATION",
        "expectedEffect": "수분 보충에 도움을 줄 수 있습니다.",
        "sortOrder": 1,
        "completed": false,
        "completedAt": null
      }
    ]
  },
  "message": "요청이 성공했습니다."
}
```

### 체크박스 토글

```http
PATCH /api/v1/routines/today/items/{itemId}
```

```json
{
  "success": true,
  "data": {
    "itemId": 1001,
    "completed": true
  },
  "message": "요청이 성공했습니다."
}
```

## 9. Achievement

```http
GET /api/v1/achievements/summary
```

```json
{"success":true,"data":{"weeklyPerformanceRate":60,"previousWeekDiff":10,"avgCompletedCount":4.5,"streakDays":2},"message":"요청이 성공했습니다."}
```

```http
GET /api/v1/achievements/weekly-trend
```

```json
{"success":true,"data":{"days":[{"date":"2026-08-17","completedCount":4,"totalCount":8}]},"message":"요청이 성공했습니다."}
```

```http
GET /api/v1/achievements/history
```

```json
{"success":true,"data":{"weeks":[{"weekStart":"2026-08-10","weekEnd":"2026-08-16","performanceRate":60,"completedCount":24,"totalCount":40}]},"message":"요청이 성공했습니다."}
```

## 10. 반드시 확인할 실패 케이스

### 인증 누락

보호 API를 Authorize 없이 호출한다.

```text
401 Unauthorized
```

### 단계 순서 위반

Step0 또는 Step1을 건너뛰고 다음 단계를 호출한다.

```json
{
  "code": "ONBOARDING_STEP_ORDER_INVALID",
  "message": "이전 온보딩 단계를 먼저 완료해야 합니다.",
  "fieldErrors": []
}
```

### 잘못된 Master Code

```json
{
  "skinType": "DRY",
  "skinConcerns": ["UNKNOWN"],
  "ownedTools": [],
  "bodyConcerns": [],
  "bodyGoals": ["MAINTAIN"]
}
```

```json
{
  "code": "INVALID_SKIN_CONCERN",
  "message": "존재하지 않거나 사용할 수 없는 피부 고민입니다.",
  "fieldErrors": []
}
```

### 알림 ON이지만 시각 누락

```json
{"notificationEnabled":true}
```

```json
{
  "code": "INVALID_NOTIFICATION_SETTINGS",
  "message": "알림 설정이 올바르지 않습니다.",
  "fieldErrors": []
}
```

### OpenAI 설정 누락

```text
503 AI_PROVIDER_NOT_CONFIGURED
```

### OpenAI 또는 응답 검증 실패

```text
502 ROUTINE_GENERATION_FAILED 또는 AI_RESPONSE_INVALID
```

실패하면 IntelliJ Console에서 `errorCode`, root cause, OpenAI 응답 로그를 확인한다. API Key와 Authorization token은 로그나 이슈에 첨부하지 않는다.
