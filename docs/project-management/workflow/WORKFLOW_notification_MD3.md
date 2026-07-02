## Step 1: 참여 촉진 배치 (리텐션) — notification-svc 역할
### 1.1 알림 인프라 (Build 1)
- [x] device 모듈(FCM 토큰 등록/해제 `POST`/`DELETE /notifications/devices`, 멱등 upsert + IDOR 방지) — platform-svc에서 이관(2026-07-01, PR #3)
- [x] inbox 모듈(인앱 알림 저장·조회, `WelcomeNotificationConsumer`가 `UserRegisteredEvent` 구독) — 이관(PR #3)
- [x] gateway `/notifications/**` 라우트 — 기존 존재(확인)
### 1.2 알림 설정 스키마·내부 API (Build 2)
- [x] `user_notification_prefs` 테이블(shared `V202607021002`) + timezone 내부 bulk 조회 `GET /notifications/internal/prefs/timezones` — 완료(2026-07-02, PR #5). learning-svc 스트릭 롤오버 스케줄러가 소비
### 1.3 사용자 대면 설정 + 일일 리마인더 (Build 3)
- [x] `GET/PUT /notifications/prefs/me`(timezone·선호시간대·리마인더/이메일 on-off, IANA·`HH:mm` 검증, 유저별 upsert) — 완료(2026-07-02, PR #6)
- [x] `PushSender` 추상화 + `InboxPushSender`(인앱 inbox 전달; 실제 FCM은 후속) — 완료(PR #6)
- [x] `PreferredTimeReminderScheduler`(10분 주기 TZ 윈도우 스캔) + `DailyReminderService`(inbox 기반 당일 1회 중복방지) + `@EnableScheduling` — 완료(PR #6)
- [x] 전체 스위트 `./gradlew build` 28/28 GREEN + 원격 CI green + opus 전체 리뷰 READY(Critical/Important 0)
### 1.4 정체 탐지·주간 리포트 (Build 4~5, TARGET)
- [ ] `StagnationConsumer`(`progress.user.stagnated` 구독 → ai-svc `POST /ai/re-engagement` 동기 호출 → 대상 디바이스 푸시) — **Build 4 목표(TARGET)**
- [ ] `WeeklyReportConsumer`(`progress.report.generated` 구독 → `weekly_report` 저장 → SMTP 이메일 발송 + "리포트 도착" 푸시) — **Build 5 목표(TARGET)**
- [ ] 실제 FCM 발송(`FcmPushSender implements PushSender`) — **후속**(Firebase 서비스계정 인프라 필요). 추가 시 리마인더 당일-중복방지를 inbox 결합에서 분리 필요
