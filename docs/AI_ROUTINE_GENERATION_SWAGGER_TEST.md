# AI Routine Generation Swagger 테스트

## 1. 실행 설정

실제 OpenAI 연동은 기존 DB/JWT 환경변수에 아래 설정을 추가한다.

```text
AI_PROVIDER=openai
OPENAI_API_KEY=발급받은_API_KEY
AI_MODEL=gpt-5.6-luna
AI_LOG_RESPONSE=true
AI_PROMPT_VERSION=routine-v1
ROUTINE_GENERATION_SCHEDULER_DELAY_MS=60000
```

외부 과금 없이 DB lifecycle만 확인할 때는 `AI_PROVIDER=stub`을 사용한다.

```bash
./gradlew bootRun
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

## 2. 인증

Auth의 이메일 인증 → 회원가입 → 로그인 순서로 실행한다. 로그인 응답의 `accessToken`을 Swagger 우측 상단 `Authorize`에 입력한다. 이후 생성된 `users.id`를 확인해 아래 `{id}`에 사용한다.

## 3. Onboarding 입력

`POST /api/v1/onboarding/step1`

```json
{"height":165.3,"weight":55.2,"gender":"FEMALE","ageGroup":"TWENTIES","bodyConcerns":["FATIGUE"],"bodyGoal":"BUILD_HABIT"}
```

`POST /api/v1/onboarding/step2`

```json
{"skinType":"SENSITIVE","skinConcerns":["WRINKLE"]}
```

`POST /api/v1/onboarding/step3`

```json
{"routineTimePreference":"MORNING","routineDifficulty":"SIMPLE","notificationTime":"08:00"}
```

Weather 입력에 좌표가 필요하므로 `PATCH /api/v1/users/{id}/profile`도 실행한다.

```json
{"regionSido":"서울특별시","regionSigungu":"강남구","latitude":37.5172,"longitude":127.0473,"locationSource":"GPS"}
```

## 4. 최초 Routine 생성

`POST /api/v1/onboarding/complete`를 실행한다. 성공 응답은 `status=COMPLETED`, `lastCompletedStep=3`과 생성된 전체 AI 루틴을 함께 반환한다.

```json
{
  "success": true,
  "data": {
    "status": "COMPLETED",
    "lastCompletedStep": 3,
    "step1CompletedAt": "2026-08-16T10:00:00Z",
    "step2CompletedAt": "2026-08-16T10:01:00Z",
    "step3CompletedAt": "2026-08-16T10:02:00Z",
    "completedAt": "2026-08-16T10:03:00Z",
    "routine": {
      "routineId": 10,
      "directionText": "오늘의 관리 방향",
      "homeComment": "오늘도 작은 실천을 이어가세요.",
      "items": [
        {
          "timeSlot": "MORNING",
          "category": "SKIN",
          "title": "미온수 세안",
          "detail": "미온수로 부드럽게 세안하세요.",
          "effectCode": "CLEAN",
          "expectedEffect": "피부 청결 유지"
        }
      ]
    }
  }
}
```

확인 사항:

- `GET /api/v1/onboarding/progress`: `COMPLETED`
- `GET /api/v1/routines/today`: OpenAI가 만든 한국어 Routine Item 반환
- `daily_routines.status`: `READY`
- `notification_scheduled_at`: `NULL`
- `ai_model`: `gpt-5.6-luna`
- `prompt_version`: `routine-v1`
- personalization/performance snapshot 존재
- `routine_items.sort_order`: 1부터 순서대로 저장
- 같은 날짜에 complete를 다시 호출하면 AI를 중복 호출하지 않고 DB에 저장된 동일 루틴을 반환

IntelliJ Run 콘솔에서 다음 로그의 `response` 값으로 OpenAI가 반환한 전체 JSON을 확인한다.

```text
OpenAI routine response received. model=gpt-5.6-luna promptVersion=routine-v1 routineDate=... response={...}
```

## 5. 실패 확인

- Step3 전에 complete: `409 ONBOARDING_STEP_ORDER_INVALID`
- 위치 좌표가 없는 상태에서 complete: `404 USER_LOCATION_NOT_FOUND`이며 Routine 생성 행을 만들지 않음
- `AI_PROVIDER=unconfigured`로 실행 후 complete: `503 AI_PROVIDER_NOT_CONFIGURED`, onboarding과 routine은 `FAILED`
- `FAILED` 상태에서 원인을 수정하고 complete 재호출: 기존 Routine 행을 `GENERATING`으로 전환해 재시도 후 결과 반환

일반 `test`와 `build`는 유료 AI API를 호출하지 않는다. 실제 Provider 검증은 `OPENAI_API_KEY`를 프로세스 환경변수로 명시한 `OpenAiRoutineGenerationAdapterLiveTest`에서만 실행한다.
