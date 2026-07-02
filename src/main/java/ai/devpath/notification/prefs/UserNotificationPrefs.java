package ai.devpath.notification.prefs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * 알림 수신 설정. Build 2는 timezone만 사용한다(스트릭 롤오버 스케줄러용).
 * 스키마: devpath-shared {@code V202607021002__user_notification_prefs.sql}.
 */
@Entity
@Table(name = "user_notification_prefs")
public class UserNotificationPrefs {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Column(nullable = false)
	private String timezone = "Asia/Seoul";

	@Column(name = "preferred_time_slot", nullable = false)
	private String preferredTimeSlot = "19:00";

	@Column(name = "reminder_enabled", nullable = false)
	private Boolean reminderEnabled = true;

	@Column(name = "weekly_report_email_enabled", nullable = false)
	private Boolean weeklyReportEmailEnabled = true;

	@Column(name = "updated_at")
	private Instant updatedAt;

	public Long getUserId() { return userId; }
	public void setUserId(Long v) { this.userId = v; }
	public String getTimezone() { return timezone; }
	public void setTimezone(String v) { this.timezone = v; }
	public String getPreferredTimeSlot() { return preferredTimeSlot; }
	public Boolean getReminderEnabled() { return reminderEnabled; }
	public Boolean getWeeklyReportEmailEnabled() { return weeklyReportEmailEnabled; }
	public Instant getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(Instant v) { this.updatedAt = v; }
}
