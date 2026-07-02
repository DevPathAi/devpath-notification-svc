# devpath-notification-svc

**DevPath AI** 알림 서비스 — FCM 디바이스 토큰 등록, 인앱 알림 인박스, 알림 설정(timezone·선호시간대), 선호시간대 일일 리마인더. notification-svc의 남은 참여 촉진 기능인 정체 탐지 재참여 푸시(Build 4)·주간 리포트 이메일 발송(Build 5)은 아직 구현되지 않은 후속 **목표(TARGET)**다.

## 도메인

| 모듈 | 역할 |
|------|------|
| device | FCM 디바이스 토큰 등록/해제 |
| inbox | 인앱 알림(웰컴·리마인더 등) 저장·조회, `UserRegisteredEvent` 등 구독 |
| prefs | 알림 설정(timezone·선호시간대·리마인더/이메일 on-off): 사용자 대면 `GET/PUT /notifications/prefs/me` + 내부 timezone bulk 조회 |
| push | 전달 채널 추상화(`PushSender`) — 현재 구현은 인앱 inbox 저장, FCM 발송은 후속 |
| reminder | 선호시간대 일일 리마인더 스케줄러(TZ 윈도우 스캔, 하루 1회) |

## 구성

- Spring Boot 4.0.x · Java 21 · Gradle (Kotlin DSL)
- 스타터: WebMVC, Actuator, Validation, JPA, Security(OAuth2 Resource Server), Kafka
- `docs/project-management/` — [workflow-dashboard](https://github.com/DevPathAi/workflow-dashboard) 동기화 대상 디렉터리

## 빌드 / 실행

```bash
./gradlew build        # 빌드 + 테스트
./gradlew bootRun      # 로컬 실행 (기본 포트 8080)
```

로컬 인프라(PostgreSQL, Kafka 등)는 [devpath-shared](https://github.com/DevPathAi/devpath-shared)의 docker-compose를 사용한다.

## 개발 규칙

- Git 규칙: [documents/09_Git_규칙_정의서](https://github.com/DevPathAi/documents/blob/main/09_Git_규칙_정의서.md)
- 코드 리뷰: [documents/12_코드_리뷰_규칙](https://github.com/DevPathAi/documents/blob/main/12_코드_리뷰_규칙.md)
- 테스트 전략: [documents/11_테스트_전략서](https://github.com/DevPathAi/documents/blob/main/11_테스트_전략서.md)
