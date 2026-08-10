# Routia Backend Architecture

## 1. 서비스 핵심 흐름

Routia의 핵심 비즈니스는 사용자의 개인화 정보, 현재 환경 데이터, 과거 수행 데이터를 조합해 AI 기반 데일리 안티에이징 루틴을 생성하고, 사용자의 수행 결과를 다음 루틴 생성에 다시 반영하는 피드백 루프다.

```text
회원가입/로그인
-> 온보딩
-> 사용자 프로필/니즈/선호/위치 저장
-> 온보딩 완료 직후 최초 루틴 즉시 생성
-> 홈에서 오늘 루틴 확인
-> 사용자가 루틴 아이템 수행 체크
-> 수행 기록 축적
-> 성취도 조회
-> 다음 루틴 생성 시 전날/최근 7일 수행 패턴 반영
```

오늘 생성된 루틴은 사용자 프로필이나 니즈가 당일 수정되어도 변경하지 않는다. 변경 사항은 다음 루틴 생성부터 반영한다. 사용자당 날짜별 루틴은 하나만 존재하고, 당일 재생성 기능은 제공하지 않는다.

## 2. Domain Classification

### Core Domain

- `routine`: 개인화 AI 루틴 생성, 데일리 루틴, 루틴 아이템 수행, 수행 피드백 분석
- 핵심 경쟁력은 "개인화 입력 + 환경 입력 + 행동 피드백 -> 다음 루틴 개선" 흐름이다.

### Supporting Domain

- `personalization`: 신체/피부/목표/선호/위치 등 루틴 생성에 필요한 사용자 개인화 정보
- `onboarding`: 단계별 입력 상태 관리와 최초 루틴 생성 트리거
- `weather`: 사용자의 저장 위치 기반 날씨/자외선 정보 제공
- `notification`: 알림 희망 시간, FCM 디바이스, 발송 로그 관리
- `achievement`: 루틴 수행 기록에서 진행률, 추이, 연속 달성일을 계산하는 조회 기능

### Generic Domain

- `auth`: 회원가입, 로그인, 이메일 인증, 계정 상태
- `global`: 공통 설정, 인증 기반, 에러 처리, 공통 타입

## 3. Bounded Context

| Context | Responsibility | Notes |
| --- | --- | --- |
| `auth` | 가입, 로그인, 이메일 인증, 계정 상태와 soft delete 정책 | 프로필/니즈를 소유하지 않는다. |
| `personalization` | 사용자 신체 정보, 피부 정보, 목표, 고민, 루틴 선호, 기본 위치 | 루틴 생성 입력 snapshot의 원천이다. |
| `onboarding` | 온보딩 단계 진행 상태와 완료 처리 | 완료 시 personalization 저장을 보장하고 최초 routine generation을 호출한다. |
| `routine` | 데일리 루틴 생성, 루틴 아이템 수행, 수행 패턴 요약, snapshot 보존 | AI는 이 Context의 outbound port 뒤에 둔다. |
| `weather` | 저장된 위치 기준 날씨/자외선 조회 | 외부 Weather API는 infrastructure adapter다. |
| `notification` | PushDevice, 알림 시간, 발송 요청, 발송 로그 | FCM은 outbound port 뒤에 둔다. |
| `achievement` | today/week/streak/history 계산 | 별도 원본 Aggregate를 만들지 않고 routine records를 읽는다. |
| `global` | cross-cutting concern | 비즈니스 규칙을 넣지 않는다. |

## 4. Context Map

```text
auth
  -> onboarding

onboarding
  -> personalization
  -> weather
  -> routine generation

routine generation
  -> personalization snapshot input
  -> weather snapshot input
  -> performance summary input
  -> AI provider port

routine execution
  -> routine item completion records
  -> performance summary for next generation

achievement
  -> routine read model

notification
  -> routine generated event/read model
  -> FCM provider port
```

### Main Flow

1. `auth`가 가입/로그인과 계정 상태를 처리한다.
2. `onboarding`은 단계별 입력을 받아 `personalization`에 프로필, 니즈, 선호, 위치를 저장한다.
3. `onboarding.complete`는 최초 루틴 생성을 즉시 트리거한다.
4. `routine.application`은 personalization snapshot, weather snapshot, 전날/최근 7일 performance summary를 조합한다.
5. `routine.infrastructure`의 AI adapter가 외부 AI API를 호출한다.
6. `routine.domain`은 날짜별 루틴 1개, 난이도별 최대 아이템 수, snapshot 보존 규칙을 지킨다.
7. `notification`은 생성 완료 후 사용자의 알림 희망 시간에 FCM 발송을 예약/수행한다.
8. `routine execution`의 완료 여부와 완료 시각이 다음 루틴 생성을 위한 Source of Truth가 된다.
9. `achievement`는 `daily_routines`와 `routine_items`를 읽어 진행률, 주간 추이, 연속 달성일을 계산한다.

### Circular Dependency 방지

- Context 간 직접 domain model 참조를 금지한다.
- 다른 Context의 데이터가 필요하면 application port 또는 query DTO를 통해 읽는다.
- `routine`은 Weather API, AI API, FCM 구현체를 직접 알지 않는다.
- `achievement`는 routine 수행 데이터를 읽지만 routine aggregate를 변경하지 않는다.
- `onboarding`은 완료 orchestration만 담당하고, personalization/routine의 내부 규칙을 소유하지 않는다.

## 5. Aggregate / Entity / Value Object Candidates

### Aggregate Root Candidates

- `auth.domain.Account`: 계정 식별자, 이메일, 비밀번호 해시, 계정 상태, soft delete
- `auth.domain.EmailVerification`: 이메일 인증 코드 발급/검증/만료
- `personalization.domain.UserPersonalization`: 프로필, 목표, 피부/신체 고민, 루틴 선호, 기본 위치
- `onboarding.domain.OnboardingProgress`: 단계별 완료 여부와 완료 가능 여부
- `routine.domain.DailyRoutine`: 특정 사용자와 날짜의 루틴, routine items, generation snapshots
- `notification.domain.PushDevice`: 사용자별 브라우저/디바이스 FCM token
- `notification.domain.NotificationDeliveryLog`: FCM 발송 성공/실패 추적

### Entity Candidates

- `RoutineItem`: 시간대, 카테고리, title, detail, effect, 완료 여부, 완료 시각
- `BodyConcern` / `SkinConcern`: MVP에서는 enum 또는 reference data로 시작하고, 운영 관리가 필요해질 때 catalog entity로 승격한다.
- `PushDevice`: token, platform/browser, 활성 여부, 마지막 사용 시각
- `NotificationDeliveryLog`: 발송 대상, 발송 시각, 결과, 실패 사유

### Value Object Candidates

- `UserId`, `RoutineId`, `RoutineItemId`
- `Height`, `Weight`, `AgeRange`, `Gender`
- `BodyGoal`, `BodyConcernCode`, `SkinType`, `SkinConcernCode`
- `RoutinePreferenceTime`, `RoutineDifficulty`, `NotificationTime`
- `Location`, `LocationSource`, `Latitude`, `Longitude`
- `WeatherSnapshot`, `UvIndex`
- `PersonalizationSnapshot`, `PerformanceSnapshot`
- `CompletionRate`, `RoutineDate`, `RoutineTimeSlot`, `RoutineCategory`

### Domain Service / Policy Candidates

- `OnboardingCompletionPolicy`: 모든 필수 step 충족 여부
- `RoutineGenerationPolicy`: 사용자당 날짜별 1개, 당일 재생성 금지, difficulty별 최대 item 수
- `PerformanceSummaryPolicy`: 전날과 최근 7일 수행 패턴 계산
- `AchievementPolicy`: 2/3 이상 완료 시 달성, 루틴 없는 날은 평균 제외 및 streak 단절
- `NotificationSchedulingPolicy`: 알림 희망 시간 전에 루틴 생성, 지정 시간에 FCM 발송
- `ActiveAccountPolicy`: 정상 조회에서 soft deleted/withdrawn 계정 제외

## 6. External System Ports

외부 시스템은 domain이 아니라 infrastructure adapter다. Application layer는 port interface에만 의존한다.

| Port | Owner Context | Adapter Example |
| --- | --- | --- |
| `RoutineGenerationPort` | `routine.application` | OpenAI 또는 다른 AI API adapter |
| `WeatherProviderPort` | `weather.application` | External Weather API adapter |
| `PushNotificationPort` | `notification.application` | Firebase Cloud Messaging adapter |
| `EmailSenderPort` | `auth.application` | SMTP 또는 Email provider adapter |
| `PasswordEncoderPort` | `auth.application` | Spring Security BCrypt adapter |
| `TokenProviderPort` | `auth.application` | JWT 또는 session token adapter |

AI에게 DB 접근 권한을 주지 않는다. Routine Application이 필요한 데이터를 조합해 AI 요청 모델을 만들고, AI 결과를 domain 규칙에 맞게 검증한 뒤 저장한다.

## 7. Package Structure

```text
src/main/java/com/routiaback
├── RoutiaBackApplication.java
├── auth
│   ├── presentation      # Auth REST controllers, request/response DTOs, validation
│   ├── application       # Signup/login/email verification use cases and ports
│   ├── domain            # Account, email verification, account status rules
│   └── infrastructure    # Persistence, email sender, password/token adapters
├── personalization
│   ├── presentation      # /users/me/profile, /users/me/needs APIs
│   ├── application       # Profile/needs/preference/location commands and queries
│   ├── domain            # Personalization aggregate, concerns, preferences, location
│   └── infrastructure    # JPA persistence and reference data adapters
├── onboarding
│   ├── presentation      # Onboarding step/progress/complete APIs
│   ├── application       # Step orchestration and initial routine generation trigger
│   ├── domain            # Onboarding progress and completion policy
│   └── infrastructure    # Onboarding progress persistence
├── routine
│   ├── presentation      # Home, today routine, routine item check APIs
│   ├── application       # Routine generation/execution/performance use cases
│   ├── domain            # DailyRoutine aggregate, RoutineItem, snapshots, policies
│   └── infrastructure    # Routine persistence and AI provider adapter
├── weather
│   ├── presentation      # Today weather API
│   ├── application       # Weather query use cases and provider port
│   ├── domain            # Weather, UV index, weather snapshot value objects
│   └── infrastructure    # External Weather API adapter and persistence
├── notification
│   ├── presentation      # Push device and notification preference APIs
│   ├── application       # Schedule/send/log notification use cases
│   ├── domain            # PushDevice, delivery log, notification policies
│   └── infrastructure    # FCM adapter and notification persistence
├── achievement
│   ├── presentation      # Summary, weekly trend, history APIs
│   ├── application       # Achievement read/query use cases
│   ├── domain            # Calculation policies, no persisted achievement aggregate
│   └── infrastructure    # Routine read adapters for achievement views
└── global
    ├── config            # Spring configuration
    ├── security          # Security configuration and current user support
    ├── error             # Exception handling and error response
    └── common            # Stable shared primitives only
```

## 8. Layer Responsibility

### presentation

- REST Controller
- Request/Response DTO
- Validation
- HTTP status/header/body conversion
- Application use case 호출

### application

- UseCase/Application Service
- Transaction boundary
- Command/Query model
- Context 내부 orchestration
- 다른 Context 또는 외부 시스템과 통신하기 위한 port 정의

### domain

- Aggregate, Entity, Value Object
- Domain Service, Domain Policy
- Repository interface
- Domain exception
- Business invariant

### infrastructure

- JPA entity and Spring Data repository
- Repository implementation and persistence adapter
- External API adapter
- FCM, Email, AI, Weather provider integration
- Spring-specific implementation

## 9. Dependency Rules

### Allowed

```text
presentation -> application
application -> domain
application -> application port
infrastructure -> application port
infrastructure -> domain
global config/security/error -> application or infrastructure wiring
```

### Forbidden

```text
domain -> presentation
domain -> application
domain -> infrastructure
domain -> Spring/JPA annotations
domain -> request/response DTO
context A domain -> context B domain
routine -> AI/Weather/FCM concrete adapter
achievement -> routine write use case
global -> business rule owner
```

Cross-context communication must go through application-level ports, use cases, events, or query DTOs. Do not import another Context's aggregate to reuse fields.

## 10. Aggregate Design Rules

- DB table and Aggregate Root are not 1:1.
- Create an Aggregate Root only when it owns invariants and lifecycle.
- `DailyRoutine` owns `RoutineItem` because item completion belongs to a specific daily routine.
- `Achievement` is not an Aggregate Root in the current MVP because source data is routine execution records.
- Concern code tables may start as enum/reference data. Do not create behaviorless aggregate roots for them.
- Snapshot data is immutable after routine generation.
- JPA persistence models may be separated from domain models when annotations or relational mapping would pollute domain code.

## 11. Snapshot Policy

`daily_routines` preserves the inputs used at generation time:

- `personalizationSnapshot`: profile, skin/body needs, goals, routine preference, difficulty at generation time
- `performanceSnapshot`: 전날 수행률, 미완료 카테고리, 최근 7일 평균 수행률, 잘/못 수행하는 시간대 등
- `weatherSnapshot`: 루틴 생성 시점에 참조한 날씨와 자외선 정보

Snapshots are not the source of truth. Source of truth for execution is `RoutineItem` completion state and completed time. Statistics are recalculated from routine execution records when needed, then stored as snapshot only to explain what the AI used for a specific routine.

## 12. Initial Development Rules

- Do not create global `controller/`, `service/`, `repository/`, `entity/` packages.
- Do not put Spring/JPA annotations in the domain layer unless the team explicitly chooses an Active Record style later.
- Do not let `personalization` become a catch-all User domain.
- Do not let `onboarding` own profile, routine, weather, or notification rules.
- Do not let `routine` call external AI/Weather/FCM implementations directly.
- Do not pass presentation DTOs into application or domain layers.
- Do not add empty Service/Repository classes just to mirror a table.
- Add abstractions only when they protect a real dependency boundary or invariant.

## 13. Review Checklist

- Does each package represent a business capability rather than a DB table?
- Can `routine` generate a routine without knowing concrete AI or Weather clients?
- Can today's routine remain unchanged after profile changes?
- Is the "one user, one date, one routine" rule enforced in the routine domain/application?
- Is performance summary derived from routine item execution records?
- Is achievement calculated from routine records without becoming a write aggregate?
- Are deleted/withdrawn accounts excluded from normal account lookups?
- Is every external system behind a port?
- Are snapshots immutable after generation?
