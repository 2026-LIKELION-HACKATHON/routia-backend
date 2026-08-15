# AI Routine Generation PHASE 1 구현 보고서

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

저장소에 Provider/Model/API 계약이 확정되어 있지 않아 특정 유료 Provider Adapter를 임의로 추가하지 않았다.

- 기본값 `AI_PROVIDER=unconfigured`: 실제 생성 요청은 `AI_PROVIDER_NOT_CONFIGURED`로 실패하고 Routine은 `FAILED`가 된다.
- 개발 확인용 `AI_PROVIDER=stub`: 외부 과금 없이 전체 Onboarding/DB lifecycle을 검증한다.
- 실제 AI 결과와 Prompt 튜닝은 Provider/Model 확정 및 API Key 설정 후 진행해야 한다.

## DB/API 변경

- Production DDL 변경 없음. 기존 `weather_snapshots`, `daily_routines`, `routine_items`, `routine_schedules`, `onboarding_progress`를 사용한다.
- Public API 추가: `POST /api/v1/onboarding/complete`
- FCM/PushDevice/NotificationLog는 PHASE 2이므로 구현하지 않았다.
