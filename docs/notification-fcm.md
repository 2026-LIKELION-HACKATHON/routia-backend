# Daily Routine Scheduling + Firebase Web FCM

## 동작 요약

- 알림 ON: 알림 시각 10분 전에 다음 일자 루틴을 생성하고 `notification_scheduled_at`에 알림 시각을 저장한다.
- 알림 OFF/미설정: 저장된 알림 시각과 무관하게 기본 시각 00:00에 루틴을 생성하고 `notification_scheduled_at`은 `NULL`로 저장한다.
- 온보딩 완료 직후 생성되는 최초 루틴은 알림 대상이 아니다.
- Notification Scheduler는 `READY`이면서 발송 예정 시각이 지난 루틴만 조회한 뒤 발송 직전에 현재 알림 설정과 Device 소유권을 다시 확인한다.
- FCM 성공/실패는 `notification_logs`에 `SENT`/`FAILED`로 기록되며 루틴의 `READY` 상태를 변경하지 않는다.
- `(routine_id, push_device_id, notification_type)` unique constraint가 다중 인스턴스의 중복 발송을 차단한다.

## Firebase Target

Backend는 Firebase Admin Java SDK의 `Message.Builder.setFid()`를 사용한다. Frontend는 Firebase Installation ID(FID)를 발급받아 Backend에 전달해야 하며 Backend는 FID를 생성하지 않는다.

## API

모든 요청은 `Authorization: Bearer {accessToken}`이 필요하다.

### Web Push Device 등록/재활성화

```http
POST /api/v1/users/{userId}/push-devices
Content-Type: application/json

{
  "installationId": "FIREBASE_INSTALLATION_ID",
  "platform": "WEB"
}
```

같은 FID를 다시 등록하면 row를 추가하지 않고 활성화한다. 다른 사용자가 같은 FID를 등록하면 소유권은 현재 인증 사용자에게 이전된다. 응답과 로그에는 FID를 포함하지 않는다.

### Web Push Device 비활성화

```http
DELETE /api/v1/users/{userId}/push-devices/{deviceId}
```

row를 삭제하지 않고 `is_active=false`로 변경한다.

## Local Credential

서비스 계정 JSON은 저장소 밖에 보관하고 소유자만 읽을 수 있도록 권한을 제한한다.

```bash
chmod 600 /secure/path/firebase-service-account.json
export GOOGLE_APPLICATION_CREDENTIALS=/secure/path/firebase-service-account.json
export NOTIFICATION_PROVIDER=firebase
```

이 Repository는 Git에서 제외되는 `.env.firebase`도 읽을 수 있다. 로컬 파일에는 JSON 내용이나 private key를 복사하지 않고 provider와 `GOOGLE_APPLICATION_CREDENTIALS` 경로만 기록한다.

Production에서는 JSON을 Repository 또는 Docker image에 포함하지 않고 배포 환경의 Secret Manager, mounted secret, IAM 또는 Workload Identity를 사용한다.

## 설정

```properties
NOTIFICATION_PROVIDER=firebase
GOOGLE_APPLICATION_CREDENTIALS=/secure/path/firebase-service-account.json
NOTIFICATION_SCHEDULER_DELAY_MS=60000
ROUTINE_GENERATION_LEAD_MINUTES=10
ROUTINE_DEFAULT_GENERATION_TIME=00:00
ROUTINE_DEFAULT_TIMEZONE=Asia/Seoul
```

## DB Migration

신규 DB에는 [notification-fcm.sql](database/notification-fcm.sql)을 적용한다.

이미 구형 `push_devices.fcm_token`이 존재하는 현재 RDS에는
[notification-fcm-existing-rds.sql](database/notification-fcm-existing-rds.sql)을 한 번 적용한다.
이 migration은 기존 컬럼을 `firebase_installation_id`로 변경하고 누락된
`notification_logs`를 생성한다. 이미 존재하는
`idx_daily_routines_status_notification` 인덱스는 재생성하지 않는다.

- `push_devices.firebase_installation_id`는 FID를 저장한다.
- FID는 전역 unique이므로 같은 브라우저가 다른 사용자에게 등록될 때 잘못된 사용자에게 Push가 전달되지 않는다.
- `notification_logs`의 delivery unique constraint가 중복 발송을 차단한다.

## 테스트

일반 테스트와 빌드는 외부 Firebase를 호출하지 않는다.

```bash
./gradlew test
./gradlew clean build
```

Firebase Admin 초기화 및 실제 FID 발송은 별도 태스크로 실행한다.

```bash
GOOGLE_APPLICATION_CREDENTIALS=/secure/path/firebase-service-account.json \
TEST_FIREBASE_FID={frontend-issued-fid} \
./gradlew externalFcmTest
```

credential만 있으면 Firebase Admin 초기화 테스트를 실행하고 실제 FID가 없으면 발송 테스트는 skip한다. 실제 Firebase message ID, credential, private key와 FID는 로그에 출력하지 않는다.
