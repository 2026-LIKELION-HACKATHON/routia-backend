# Daily Routine Scheduling + Web FCM

## 동작 요약

- 알림 ON: 알림 시각 10분 전에 다음 일자 루틴을 생성하고, `notification_scheduled_at`에 알림 시각을 저장한다.
- 알림 OFF/미설정: 알림 시각이 DB에 남아 있어도 매일 06:00에 루틴을 생성하고 `notification_scheduled_at`은 `NULL`로 저장한다.
- 온보딩 완료 직후 생성되는 최초 루틴은 알림 대상이 아니다.
- Notification Scheduler는 `READY`이면서 발송 예정 시각이 지난 루틴만 조회한 뒤, 발송 직전에 현재 알림 설정을 다시 확인한다.
- FCM 성공/실패는 `notification_logs`에 `SENT`/`FAILED`로 기록되며 루틴의 `READY` 상태를 변경하지 않는다.
- `(routine_id, push_device_id, notification_type)` unique constraint가 다중 인스턴스의 중복 발송을 차단한다.

## API

모든 요청은 `Authorization: Bearer {accessToken}`이 필요하다.

### Web Push 기기 등록/재활성화

```http
POST /api/v1/users/{userId}/push-devices
Content-Type: application/json

{
  "token": "FCM_WEB_REGISTRATION_TOKEN",
  "platform": "WEB"
}
```

같은 token을 다시 등록하면 row를 추가하지 않고 활성화한다. 다른 사용자가 같은 브라우저 token을 등록하면 token 소유권은 현재 인증 사용자에게 이전된다. 응답에는 token을 포함하지 않는다.

### Web Push 기기 비활성화

```http
DELETE /api/v1/users/{userId}/push-devices/{deviceId}
```

row를 삭제하지 않고 `is_active=false`로 변경한다.

## 배포 설정

배포 전에 [notification-fcm.sql](database/notification-fcm.sql)을 Production DB에 적용한다.

```properties
NOTIFICATION_PROVIDER=firebase
FIREBASE_CREDENTIALS_BASE64={Firebase service-account JSON의 Base64 값}
NOTIFICATION_SCHEDULER_DELAY_MS=60000
ROUTINE_GENERATION_LEAD_MINUTES=10
ROUTINE_DEFAULT_GENERATION_TIME=06:00
ROUTINE_DEFAULT_TIMEZONE=Asia/Seoul
```

credential JSON 파일, private key, FCM token은 Git에 추가하지 않는다. `NOTIFICATION_PROVIDER` 기본값은 `unconfigured`이므로 Firebase credential이 없는 일반 테스트와 로컬 실행에서 실제 Push를 발송하지 않는다.

## 테스트

일반 테스트와 빌드는 외부 FCM을 호출하지 않는다.

```bash
./gradlew test
./gradlew clean build
```

실제 브라우저 token으로 외부 FCM을 검증할 때만 다음 전용 태스크를 실행한다.

```bash
FIREBASE_CREDENTIALS_BASE64='{base64}' \
FCM_TEST_TOKEN='{web-registration-token}' \
./gradlew externalFcmTest
```

두 환경변수 중 하나라도 없으면 외부 테스트는 skip된다. Frontend는 Firebase Messaging 초기화와 알림 권한 동의 후 얻은 registration token을 기기 등록 API로 전달해야 한다.
