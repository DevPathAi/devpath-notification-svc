package ai.devpath.notification.report;

import ai.devpath.notification.prefs.UserNotificationPrefs;
import ai.devpath.notification.prefs.UserNotificationPrefsRepository;
import ai.devpath.notification.push.PushSender;
import ai.devpath.shared.event.WeeklyReportGeneratedEvent;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** progress.report.generated 구독 → weekly_report 저장(멱등) → (설정 시)이메일 → "리포트 도착" 푸시. */
@Component
public class WeeklyReportConsumer {

	private static final Logger log = LoggerFactory.getLogger(WeeklyReportConsumer.class);
	private static final String TYPE = "WEEKLY_REPORT";

	private final WeeklyReportRepository reports;
	private final UserNotificationPrefsRepository prefs;
	private final EmailSender emailSender;
	private final PushSender pushSender;
	private final JsonMapper jsonMapper;

	public WeeklyReportConsumer(WeeklyReportRepository reports, UserNotificationPrefsRepository prefs,
			EmailSender emailSender, PushSender pushSender, JsonMapper jsonMapper) {
		this.reports = reports;
		this.prefs = prefs;
		this.emailSender = emailSender;
		this.pushSender = pushSender;
		this.jsonMapper = jsonMapper;
	}

	@KafkaListener(topics = WeeklyReportGeneratedEvent.EVENT_TYPE, groupId = "devpath-notification")
	public void onWeeklyReport(String payload) {
		WeeklyReportGeneratedEvent event;
		try {
			event = jsonMapper.readValue(payload, WeeklyReportGeneratedEvent.class);
		} catch (Exception e) {
			log.warn("WeeklyReportGeneratedEvent 역직렬화 실패 — skip: {}", payload, e);
			return; // poison 방지
		}
		if (reports.existsByUserIdAndWeekOf(event.userId(), event.weekOf())) return; // 멱등(UNIQUE 방어선과 병행)

		String subject = "이번 주 학습 리포트 (" + event.weekOf() + ")";
		String body = "스트릭 " + event.streakDays() + "일 · 진척률 " + event.progressPercent() + "%"
				+ (event.badgesEarnedThisWeek().isEmpty() ? "" : " · 이번주 배지 " + String.join(", ", event.badgesEarnedThisWeek()))
				+ (event.nextTaskTitle() == null ? "" : "\n다음 과제: " + event.nextTaskTitle());

		WeeklyReport report = new WeeklyReport();
		report.setUserId(event.userId());
		report.setWeekOf(event.weekOf());
		report.setPayload(payload);

		boolean emailEnabled = prefs.findById(event.userId())
				.map(UserNotificationPrefs::getWeeklyReportEmailEnabled).orElse(Boolean.TRUE);
		if (Boolean.TRUE.equals(emailEnabled)) {
			try {
				emailSender.send(event.userId(), subject, body);
				report.setEmailSentAt(Instant.now());
			} catch (RuntimeException e) {
				log.warn("주간 리포트 이메일 실패 — userId={} (푸시는 계속)", event.userId(), e);
			}
		}
		pushSender.send(event.userId(), TYPE, "주간 학습 리포트가 도착했어요", body);
		report.setPushSentAt(Instant.now());

		try {
			reports.save(report);
		} catch (org.springframework.dao.DataIntegrityViolationException dup) {
			// 동시 소비 레이스 — UNIQUE(user_id, week_of) 위반 = 이미 저장됨. 무시(멱등).
		}
	}
}
