# booking-service

First Ticket 프로젝트의 예매 서비스.  
좌석 선점부터 예매 생성, 결제 연동, 취소까지 전체 예매 흐름을 오케스트레이션한다.

---

## 📌 핵심 기능

### 예매 흐름

1. 대기열 서비스에서 발급받은 입장 토큰으로 예매 세션 생성 (Entry Token 검증 + Redis SET NX 블랙리스트 등록)
2. 좌석 선점 요청 → Redisson 분산락으로 동시성 제어 → Redis TTL 10분 기반 선점 등록
3. 예매 생성 + 결제 레코드 생성 (Feign 동기 호출)
4. 사용자 Toss Payments 결제 진행
5. `payment.completed` 수신 → 예매 확정 + 좌석 RESERVED (내부 메서드 호출)
6. 결제 실패 시 TTL 만료로 좌석 선점 자동 해제

### 예매 취소 흐름

1. 사용자 예매 취소 요청 → `booking.cancel.requested` 발행
2. 결제 서비스 환불 처리 → `payment.refund.completed` 발행
3. 예매 취소 확정 → `booking.cancel.confirmed` 발행
4. 좌석 상태 AVAILABLE 복구

### 좌석 선점 설계

- HELD 상태를 DB에 저장하지 않고 Redis TTL로만 관리
- TTL 만료 시 자동 해제, 별도 스케줄러 불필요
- Redisson 이중 분산락으로 동시성 문제 분리 제어
    - 사용자 레벨 락: 동일 사용자 중복 요청 직렬화
    - 좌석 레벨 락: 다른 사용자의 동일 좌석 동시 선점 차단

#### Redis 키 구조

| 키 | 값 | TTL |
| --- | --- | --- |
| `held:{seatId}` | `userId:sessionId` | 10분 |
| `session:{sessionId}` | `[seatIds]` | 10분 |
| `user-hold:{userId}:{scheduleId}` | `sessionId` | 10분 |
| `lock:{seatId}` | - | Watchdog |
| `lock:user-hold:{userId}:{scheduleId}` | - | Watchdog |

### 외부 이벤트 연동

| 이벤트 | 발행 서비스 | 처리 |
| --- | --- | --- |
| `schedule.created` | program-service | 좌석 벌크 생성 (JDBC Batch Insert) |
| `payment.completed` | payment-service | 예매 확정 + 좌석 RESERVED |
| `payment.failed` | payment-service | 예매 FAILED 처리 |
| `payment.refund.completed` | payment-service | 예매 취소 확정 + booking.cancel.confirmed 발행 |
| `booking.cancel.confirmed` | booking-service (self) | 좌석 AVAILABLE 복구 |

---

## 🛠 기술 스택

| 항목 | 기술 |
| --- | --- |
| 저장소 | PostgreSQL (예매·좌석) + Redis (선점·캐시·락) |
| 메시지 브로커 | Apache Kafka + Transactional Outbox 패턴 |
| 동시성 제어 | Redisson 분산락 |
| 결제 연동 | Toss Payments (Feign) |
| 캐시 | Redis (@Cacheable) |

공통 기술 스택은 [공통 README](https://github.com/first-ticket/.github/blob/main/profile/README.md#%EF%B8%8F-%EA%B8%B0%EC%88%A0-%EC%8A%A4%ED%83%9D) 참고.

---

## 📁 패키지 구조

DDD 기반 레이어드 아키텍처

```text
com.firstticket.bookingservice
├── booking/                          # Booking Aggregate (예매 관리)
│   ├── domain/                       # 도메인 모델, VO, Repository 인터페이스, 도메인 서비스 포트
│   │   ├── Booking.java
│   │   ├── BookingItem.java
│   │   ├── BookingStatus.java
│   │   ├── query/                    # 조회 전용 Repository 인터페이스, 조회 결과 DTO
│   │   └── service/                  # 도메인 서비스 포트 (PaymentOperator, SeatOperator 등)
│   ├── application/                  # 유즈케이스 서비스, Command/Result DTO
│   ├── infrastructure/               # JPA Repository, Kafka Consumer, Feign Client, 분산락 AOP
│   └── presentation/                 # REST Controller, Request/Response DTO
├── seat/                             # Seat Aggregate (좌석 관리)
│   ├── domain/                       # 도메인 모델, VO, Repository 인터페이스, 도메인 서비스 포트
│   │   ├── Seat.java
│   │   ├── SeatId.java
│   │   ├── SeatStatus.java
│   │   ├── SeatType.java
│   │   └── service/                  # SeatHoldManager, SeatManager 포트
│   ├── application/                  # 유즈케이스 서비스, Command/Result DTO
│   ├── infrastructure/               # JPA Repository, Redis (Redisson), Kafka Consumer, 캐시
│   └── presentation/                 # REST Controller, Internal Controller, Request/Response DTO
└── global/                           # 전역 설정, 토큰 처리 (Entry Token 검증, 블랙리스트)
```

---

## 🌐 API 엔드포인트

### 예매 API

| Method | Path | 설명 | 권한 |
| --- | --- | --- | --- |
| POST | `/api/v1/bookings/{programId}/session` | 예매 세션 생성 | 구매자 |
| POST | `/api/v1/bookings/schedules/{scheduleId}` | 예매 생성 | 구매자 |
| GET | `/api/v1/bookings/my/{bookingId}` | 예매 단건 조회 | 구매자(본인) |
| GET | `/api/v1/bookings` | 예매 목록 조회 | 구매자(본인) |
| POST | `/api/v1/bookings/{bookingId}/cancel` | 예매 취소 | 구매자(본인) |

### 좌석 API

| Method | Path | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/api/v1/seats/schedules/{scheduleId}` | 좌석 목록·잔여 수 조회 | 전체 |
| POST | `/api/v1/seats/schedules/{scheduleId}/hold` | 좌석 선점 | 구매자 |
| GET | `/api/v1/seats/schedules/{scheduleId}/hold` | 선점 중인 좌석 조회 | 구매자(본인) |
| DELETE | `/api/v1/seats/schedules/{scheduleId}/hold` | 선점 취소 | 구매자(본인) |
| POST | `/internal/v1/seats/remaining` | 잔여 좌석 수 조회 | System |

상세한 요청 / 응답 예시는 REST Docs 참조: `http://localhost:8083/docs/booking-api.html` / `http://localhost:8083/docs/seat-api.html`

---

## 🌐 포트

| 환경 | 포트 |
| --- | --- |
| local | 8083 |
| prod | 8080 (컨테이너 내부) |

---

## 🚀 로컬 실행

### 사전 조건

다음 인프라가 먼저 실행되어 있어야 한다.

1. **infra 레포의 docker-compose** — Redis, Kafka, PostgreSQL, Zipkin
2. **eureka-server** — 서비스 디스커버리
3. **config-server** — 설정 관리 서버
4. **program-service** — schedule.created 이벤트 발행
5. **payment-service** — 결제 연동

### 환경변수 설정

```bash
CONFIG_SERVER_USERNAME=
CONFIG_SERVER_PASSWORD=
GITHUB_USER=
GITHUB_TOKEN=
ENCRYPT_KEY=
```

`.env.example` 파일을 참고하여 `.env` 를 작성한다.

### 외부 설정

설정은 [config-repo](https://github.com/first-ticket/config-repo) 에서 관리한다.

**공통 (`application.yml`)**
- Redis / PostgreSQL / Kafka 연결 정보
- Zipkin, Prometheus
- Kafka Producer / Consumer 기본값 (idempotence, retries 등)

**booking-service 전용 (`booking-service.yml`)**
- JPA 스키마: `booking_schema` (ddl-auto: validate, Flyway 마이그레이션)
- HikariCP: maximum-pool-size 30, minimum-idle 10, connection-timeout 6000ms
- 토큰: `booking.token.entry_secret`, `booking.token.session-secret` (암호화 저장)
- 세션 토큰 유효기간: `booking.token.session-token-validity` (60m)
- Outbox 활성화: `messaging.outbox.enabled: true`
- Inbox 활성화: `messaging.inbox.enabled: true`

**환경별 (`booking-service-{profile}.yml`)**
- 포트, TTL 등 환경 의존 설정

---

## 🔍 헬스체크

```bash
curl http://localhost:8083/actuator/health
# → {"status":"UP"}
```
