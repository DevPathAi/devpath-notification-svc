package ai.devpath.notification.reminder;

import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 10분 주기로 선호시간대 도달 유저에게 리마인더를 보낸다(시간대 윈도우 스캔).
 * 테스트에서는 devpath.reminder.scheduler-enabled=false로 비활성(로직은 DailyReminderService를 직접 테스트).
 */
@Component
@ConditionalOnProperty(name = "devpath.reminder.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class PreferredTimeReminderScheduler {

	private final DailyReminderService service;

	public PreferredTimeReminderScheduler(DailyReminderService service) {
		this.service = service;
	}

	@Scheduled(fixedDelayString = "${devpath.reminder.fixed-delay-ms:600000}")
	public void run() {
		service.sendDueReminders(Instant.now());
	}
}
