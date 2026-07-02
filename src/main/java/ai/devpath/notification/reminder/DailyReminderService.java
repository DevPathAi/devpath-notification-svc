package ai.devpath.notification.reminder;

import ai.devpath.notification.inbox.NotificationRepository;
import ai.devpath.notification.prefs.UserNotificationPrefs;
import ai.devpath.notification.prefs.UserNotificationPrefsRepository;
import ai.devpath.notification.push.PushSender;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 선호시간대 도달 유저에게 하루 1회 고정 문구 리마인더를 전달한다(TZ 인식, inbox 기반 당일 중복방지). */
@Service
public class DailyReminderService {

	private static final Logger log = LoggerFactory.getLogger(DailyReminderService.class);
	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
	static final String TYPE = "DAILY_REMINDER";
	static final String TITLE = "오늘의 학습 리마인더";
	static final String BODY = "설정하신 시간이 되었어요. 오늘의 학습을 이어가 볼까요?";

	private final UserNotificationPrefsRepository prefs;
	private final NotificationRepository inbox;
	private final PushSender pushSender;

	public DailyReminderService(UserNotificationPrefsRepository prefs, NotificationRepository inbox,
			PushSender pushSender) {
		this.prefs = prefs;
		this.inbox = inbox;
		this.pushSender = pushSender;
	}

	public void sendDueReminders(Instant now) {
		for (UserNotificationPrefs p : prefs.findByReminderEnabledTrue()) {
			try {
				if (isDue(p, now)) {
					pushSender.send(p.getUserId(), TYPE, TITLE, BODY);
				}
			} catch (RuntimeException e) {
				// 잘못된 tz/slot 등 한 유저의 오류가 전체 배치를 막지 않도록 skip + 로그.
				log.warn("리마인더 판정 실패 — userId={} skip", p.getUserId(), e);
			}
		}
	}

	private boolean isDue(UserNotificationPrefs p, Instant now) {
		ZoneId zone = ZoneId.of(p.getTimezone());
		ZonedDateTime localNow = now.atZone(zone);
		LocalTime slot = LocalTime.parse(p.getPreferredTimeSlot(), HH_MM);
		ZonedDateTime slotToday = localNow.toLocalDate().atTime(slot).atZone(zone);
		if (localNow.isBefore(slotToday)) {
			return false; // 오늘 선호시간 아직 미도달
		}
		Instant startOfLocalDay = localNow.toLocalDate().atStartOfDay(zone).toInstant();
		return !inbox.existsByUserIdAndTypeAndCreatedAtGreaterThanEqual(p.getUserId(), TYPE, startOfLocalDay);
	}
}
