# Cloudtype Production 배포 가이드

## 배포 구조

- Source: Git Repository
- Build preset: Dockerfile
- Dockerfile path: `Dockerfile`
- Java build/runtime: 21
- Publish port: `8080`
- Spring profile: `prod`
- Health check: `GET /actuator/health`
- Timezone: `Asia/Seoul`

Cloudtype의 Publish Port와 `SERVER_PORT`는 반드시 같은 `8080`으로 설정한다.

## Cloudtype Environment Variables

값 자체가 비밀이 아닌 운영 설정이다. 실제 서비스 정책에 맞춰 입력한다.

```text
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DB_URL=jdbc:mysql://<RDS_ENDPOINT>:3306/<DATABASE>?serverTimezone=Asia/Seoul
JPA_DDL_AUTO=none

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<MAIL_ACCOUNT>
MAIL_FROM=<MAIL_ACCOUNT>
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
MAIL_TEST_CONNECTION=false

AI_PROVIDER=openai
AI_MODEL=<OPENAI_MODEL>
AI_REASONING_EFFORT=low
AI_MAX_OUTPUT_TOKENS=3000
AI_TIMEOUT_SECONDS=30
AI_PROMPT_VERSION=routine-v2-onboarding-v2
AI_PROMPT_RESOURCE=classpath:prompts/routine-v2.txt
AI_LOG_RESPONSE=false

NOTIFICATION_PROVIDER=firebase
NOTIFICATION_SCHEDULER_DELAY_MS=60000
ROUTINE_GENERATION_SCHEDULER_DELAY_MS=60000
ROUTINE_GENERATION_LEAD_MINUTES=10
ROUTINE_DEFAULT_GENERATION_TIME=06:00
ROUTINE_DEFAULT_TIMEZONE=Asia/Seoul

CORS_ALLOWED_ORIGINS=https://<FRONTEND_PRODUCTION_DOMAIN>
PROFILE_IMAGE_DIRECTORY=/tmp/routia-profile-images
```

여러 프론트 Origin을 허용할 때는 공백 없이 comma-separated 값으로 설정한다.

```text
CORS_ALLOWED_ORIGINS=https://www.example.com,https://example.com
```

Production에는 `*` 또는 localhost를 넣지 않는다. Preview 도메인이 필요하면 명시적인 HTTPS Origin으로 추가한다.

## Cloudtype Secrets

아래 값은 일반 Environment Variable이 아니라 Cloudtype Secret으로 등록한다.

```text
DB_USERNAME=<RDS_USERNAME>
DB_PASSWORD=<RDS_PASSWORD>
JWT_SECRET=<LONG_RANDOM_SECRET>
MAIL_PASSWORD=<MAIL_APP_PASSWORD>
OPENAI_API_KEY=<OPENAI_API_KEY>
FIREBASE_SERVICE_ACCOUNT_BASE64=<ONE_LINE_BASE64_JSON>
```

실제 값은 Dockerfile, `application-prod.properties`, GitHub Secret이 아닌 소스 파일, 배포 문서에 기록하지 않는다.

## Firebase Secret 준비

서비스 계정 JSON은 저장소 밖에 둔다. macOS에서 Cloudtype Secret용 한 줄 Base64를 생성한다.

```bash
base64 -i /secure/path/firebase-service-account.json | tr -d '\n'
```

출력값을 곧바로 `FIREBASE_SERVICE_ACCOUNT_BASE64` Secret에 붙여 넣고 파일로 저장하거나 로그에 남기지 않는다.

- Local: `GOOGLE_APPLICATION_CREDENTIALS` 파일 경로 사용 가능
- Production: `FIREBASE_SERVICE_ACCOUNT_BASE64` 필수
- Production에서 Base64가 누락되거나 손상되면 Firebase 초기화 단계에서 명확히 실패한다.

## Database

- DB: MySQL
- Driver: MySQL Connector/J
- Production Hibernate: `ddl-auto=none`
- Flyway/Liquibase: 사용하지 않음
- Migration: 배포 전에 승인된 SQL을 RDS에 수동 적용

애플리케이션 배포가 스키마를 생성하거나 변경하지 않는다. Notification 기능 배포 전에는 `push_devices`, `notification_logs`, `daily_routines.notification_scheduled_at` 관련 스키마가 RDS에 존재하는지 확인해야 한다.

RDS Security Group은 Cloudtype 실행 환경에서 3306 접근이 가능해야 한다. 코드 설정과 RDS 네트워크 허용은 별도 항목이다.

## CORS / 인증

- CORS 설정 위치: `CorsConfig` + `SecurityConfig`
- Auth: `Authorization: Bearer <JWT>`
- Cookie credential: 사용하지 않음 (`allowCredentials=false`)
- Methods: `GET`, `POST`, `PATCH`, `DELETE`, `OPTIONS`
- Headers: `Authorization`, `Content-Type`, `Accept`
- Preflight: JWT 없이 처리
- 실제 보호 API: 기존 JWT 인증 유지

Firebase Admin 발송은 Backend-to-Firebase 통신이므로 CORS와 무관하다. Push Device/Notification Settings API는 Browser-to-Backend 요청이므로 위 allowlist가 적용된다.

## Health / Swagger / Scheduler

- Health: `/actuator/health`
- Health detail: 외부에 노출하지 않음
- Swagger: 기존 정책대로 `/swagger-ui.html` 공개 유지
- Routine/Notification scheduler: Production에서도 활성
- Container timezone: `Asia/Seoul`
- 도메인 시간 계산: 기존 UTC `Clock`과 사용자 timezone 유지

Cloudtype Health Check path를 `/actuator/health`, port를 `8080`으로 설정한다.

## Build와 배포 전 검증

전체 테스트와 패키징은 분리한다. Docker build는 이미 검증된 코드를 package하므로 외부 OpenAI/Firebase 테스트를 호출하지 않고 `bootJar -x test`를 실행한다.

```bash
./gradlew clean build
docker build -t routia-back .
```

로컬에서 Production container를 검증할 때 실제 Secret은 command line에 직접 쓰지 말고 Git ignored env file이나 안전한 secret injection 방식을 사용한다.

```bash
docker run --rm --env-file /secure/path/routia-prod.env -p 8080:8080 routia-back
curl --fail http://localhost:8080/actuator/health
```

## Frontend 전달 정보

- API Base URL: Cloudtype가 발급한 HTTPS URL
- 허용 Origin: `CORS_ALLOWED_ORIGINS`에 등록한 프론트 HTTPS Origin
- 인증: `Authorization: Bearer <accessToken>`
- 기기 등록: `POST /api/v1/users/{id}/push-devices`
- 알림 설정 조회/수정: `GET/PATCH /api/v1/users/{id}/notification-settings`

## 운영 전 확인 사항

1. 실제 Frontend Production Domain을 `CORS_ALLOWED_ORIGINS`에 입력한다.
2. RDS 스키마와 Security Group 접근을 확인한다.
3. 모든 Cloudtype Secret을 등록한다.
4. Dockerfile의 Publish Port를 8080으로 설정한다.
5. `/actuator/health`가 `UP`인지 확인한다.
6. 로그인 후 Frontend Origin에서 보호 API CORS 요청을 확인한다.
7. 테스트 사용자 FID로 실제 FCM 수신을 확인한다.

프로필 이미지는 현재 Container 로컬 디렉터리에 저장되므로 재배포/재시작 시 영속성이 보장되지 않는다. Production 이미지 영속성이 필요하면 Object Storage adapter 도입이 별도 작업으로 필요하다.
