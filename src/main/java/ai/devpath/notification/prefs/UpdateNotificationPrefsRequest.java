package ai.devpath.notification.prefs;

public record UpdateNotificationPrefsRequest(
		String timezone,
		String preferredTimeSlot,
		Boolean reminderEnabled,
		Boolean weeklyReportEmailEnabled) {
}
