package ai.devpath.notification.prefs;

public record NotificationPrefsView(
		String timezone,
		String preferredTimeSlot,
		boolean reminderEnabled,
		boolean weeklyReportEmailEnabled) {

	static NotificationPrefsView from(UserNotificationPrefs p) {
		return new NotificationPrefsView(
				p.getTimezone(), p.getPreferredTimeSlot(),
				Boolean.TRUE.equals(p.getReminderEnabled()),
				Boolean.TRUE.equals(p.getWeeklyReportEmailEnabled()));
	}
}
