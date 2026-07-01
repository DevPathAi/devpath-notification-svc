# devpath-notification-svc

**DevPath AI** 알림 서비스 — FCM 디바이스 토큰 등록, 인앱 알림 인박스, 참여 촉진 배치(스트릭·주간 리포트·정체 탐지·선호 시간대 리마인더).

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
