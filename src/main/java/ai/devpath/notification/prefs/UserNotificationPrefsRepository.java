package ai.devpath.notification.prefs;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationPrefsRepository extends JpaRepository<UserNotificationPrefs, Long> {
}
