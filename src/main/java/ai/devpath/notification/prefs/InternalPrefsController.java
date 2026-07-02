package ai.devpath.notification.prefs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 서비스 간 내부 조회(게이트웨이 미경유). learning-svc의 스트릭 롤오버 스케줄러가 timezone bulk 조회에 쓴다. */
@RestController
@RequestMapping("/notifications/internal/prefs")
public class InternalPrefsController {

	private static final String DEFAULT_TIMEZONE = "Asia/Seoul";

	private final UserNotificationPrefsRepository prefs;

	public InternalPrefsController(UserNotificationPrefsRepository prefs) {
		this.prefs = prefs;
	}

	@GetMapping("/timezones")
	public List<UserTimezoneView> timezones(@RequestParam String userIds) {
		List<Long> ids = Arrays.stream(userIds.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(Long::parseLong)
				.toList();
		Map<Long, String> found = prefs.findAllById(ids).stream()
				.collect(Collectors.toMap(UserNotificationPrefs::getUserId, UserNotificationPrefs::getTimezone));
		return ids.stream()
				.map(id -> new UserTimezoneView(id, found.getOrDefault(id, DEFAULT_TIMEZONE)))
				.toList();
	}
}
