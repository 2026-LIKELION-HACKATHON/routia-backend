# AI Routine Generation PHASE 1~2 구현 보고서

## 구현 범위

- `POST /api/v1/onboarding/complete`에서 1~3단계 완료를 확인하고 오늘 루틴을 즉시 생성한다.
- 최초 루틴은 `notification_scheduled_at = null`로 저장해 FCM 대상에서 제외한다.
- 정기 생성은 `notificationTime - 10분`을 `nextGenerationAt`으로 사용한다.
- `00:05` 알림은 전날 `23:55`에 다음 날짜 루틴을 생성하도록 날짜 경계를 계산한다.
- Scheduler는 due/active 사용자별로 생성 실패를 격리하고 다음 생성 시각을 하루 뒤로 이동한다.
- AI Port, Prompt v1, Prompt Version, 구조화 응답 모델과 Backend Validation을 분리했다.
- Profile/Needs/Concern/Weather/최근 수행 데이터를 AI Request로 조합한다.
- AI에 전달한 개인화/수행 데이터를 JSON Snapshot으로 저장한다.
- `GENERATING → READY` 또는 `GENERATING → FAILED` lifecycle과 오류 코드를 저장한다.
- DB unique `(user_id, routine_date)`를 중복 생성 최종 방어선으로 사용한다.
- OpenAI Responses API와 `gpt-5.6-luna`를 실제 Provider로 연결했다.
- Structured Outputs의 strict JSON Schema로 응답 필드, enum, item 개수를 제한한다.
- Provider HTTP 오류, timeout, 미완료·거절·비정상 JSON은 루틴 생성 실패로 처리하며 Provider 응답 본문과 API Key를 노출하지 않는다.

## 트랜잭션 경계

1. 짧은 트랜잭션으로 `GENERATING` row를 확보한다.
2. 트랜잭션 밖에서 Weather와 AI를 호출한다.
3. 짧은 트랜잭션으로 Weather Snapshot, Routine Items, `READY`를 저장한다.
4. 실패하면 별도 짧은 트랜잭션으로 `FAILED`와 `generation_error_code`를 저장한다.

## Prompt

- Version: `routine-v1`
- 위치: `src/main/resources/prompts/routine-v1.txt`
- 설정: `AI_PROMPT_VERSION`, `AI_PROMPT_RESOURCE`, `AI_MODEL`, `AI_TIMEOUT_SECONDS`
- 의료 진단·처방·위험한 운동/식단을 금지하고 한국어 구조화 JSON을 요구한다.

## AI Provider 상태

- 기본값 `AI_PROVIDER=unconfigured`: 실제 생성 요청은 `AI_PROVIDER_NOT_CONFIGURED`로 실패하고 Routine은 `FAILED`가 된다.
- 개발 확인용 `AI_PROVIDER=stub`: 외부 과금 없이 전체 Onboarding/DB lifecycle을 검증한다.
- 실제 연동 `AI_PROVIDER=openai`: OpenAI Responses API를 호출한다.
- 필수 설정: `OPENAI_API_KEY`, `AI_MODEL=gpt-5.6-luna`
- 선택 설정: `OPENAI_BASE_URL`, `AI_REASONING_EFFORT`, `AI_MAX_OUTPUT_TOKENS`, `AI_TIMEOUT_SECONDS`

## 실제 연동 검증

- 합성 Profile/Needs/Weather/최근 수행 데이터로 실제 Responses API를 호출했다.
- `gpt-5.6-luna` 응답에서 `directionText`, `homeComment`, 7개 Routine Item을 확인했다.
- 날씨, 피부 타입·고민, 신체 고민, 저녁 수행률 저하가 생성 결과에 반영되었다.
- 반환된 모든 Item이 Backend Validation의 time slot, category, 필수 값, COMPLEX 최대 12개 조건을 통과했다.
- 일반 `test`/`build`에서는 유료 API를 호출하지 않고, API Key가 명시적으로 전달된 live test만 실제 호출한다.

## DB/API 변경

- Production DDL 변경 없음. 기존 `weather_snapshots`, `daily_routines`, `routine_items`, `routine_schedules`, `onboarding_progress`를 사용한다.
- Public API 추가: `POST /api/v1/onboarding/complete`
- FCM/PushDevice/NotificationLog는 이번 AI 작업에서 제외했으며 별도 `notification` 브랜치에서 구현한다.
