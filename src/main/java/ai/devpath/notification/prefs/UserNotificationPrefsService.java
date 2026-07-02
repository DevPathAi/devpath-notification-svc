package ai.devpath.notification.prefs;

import java.time.Instant;
import org.springframework.stereotype.Service;

/** 사용자 알림 설정 조회(기본값 포함)·upsert. PrefsController 및 리마인더 흐름의 진입점. */
@Service
public class UserNotificationPrefsService {

	private final UserNotificationPrefsRepository repo;

	public UserNotificationPrefsService(UserNotificationPrefsRepository repo) {
		this.repo = repo;
	}

	/** 저장된 설정이 있으면 반환, 없으면 userId만 세팅한 미저장 기본값 엔티티 반환. */
	public UserNotificationPrefs getOrDefault(long userId) {
		return repo.findById(userId).orElseGet(() -> {
			UserNotificationPrefs p = new UserNotificationPrefs();
			p.setUserId(userId);
			return p; // 나머지 필드는 엔티티 이니셜라이저 기본값
		});
	}

	/** 전체 필드 교체 upsert. */
	public UserNotificationPrefs upsert(long userId, String timezone, String preferredTimeSlot,
			boolean reminderEnabled, boolean weeklyReportEmailEnabled) {
		UserNotificationPrefs p = repo.findById(userId).orElseGet(UserNotificationPrefs::new);
		p.setUserId(userId);
		p.setTimezone(timezone);
		p.setPreferredTimeSlot(preferredTimeSlot);
		p.setReminderEnabled(reminderEnabled);
		p.setWeeklyReportEmailEnabled(weeklyReportEmailEnabled);
		p.setUpdatedAt(Instant.now());
		return repo.save(p);
	}
}
