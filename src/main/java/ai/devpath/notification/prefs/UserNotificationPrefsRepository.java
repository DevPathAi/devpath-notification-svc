package ai.devpath.notification.prefs;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationPrefsRepository extends JpaRepository<UserNotificationPrefs, Long> {
	List<UserNotificationPrefs> findByReminderEnabledTrue();
}
