package ai.devpath.notification.prefs;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 사용자 대면 알림 설정 API. 게이트웨이 /notifications/** 라우트로 노출된다(라우트는 Build 1에서 추가됨). */
@RestController
@RequestMapping("/notifications/prefs")
public class PrefsController {

	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

	private final UserNotificationPrefsService service;

	public PrefsController(UserNotificationPrefsService service) {
		this.service = service;
	}

	@GetMapping("/me")
	public NotificationPrefsView getMine(@AuthenticationPrincipal Jwt jwt) {
		return NotificationPrefsView.from(service.getOrDefault(uid(jwt)));
	}

	@PutMapping("/me")
	public ResponseEntity<NotificationPrefsView> updateMine(@AuthenticationPrincipal Jwt jwt,
			@RequestBody(required = false) UpdateNotificationPrefsRequest body) {
		if (body == null || body.timezone() == null || body.preferredTimeSlot() == null
				|| body.reminderEnabled() == null || body.weeklyReportEmailEnabled() == null
				|| !isValidZone(body.timezone()) || !isValidTimeSlot(body.preferredTimeSlot())) {
			return ResponseEntity.badRequest().build();
		}
		UserNotificationPrefs saved = service.upsert(uid(jwt), body.timezone(), body.preferredTimeSlot(),
				body.reminderEnabled(), body.weeklyReportEmailEnabled());
		return ResponseEntity.ok(NotificationPrefsView.from(saved));
	}

	private static long uid(Jwt jwt) {
		return Long.parseLong(jwt.getSubject());
	}

	private static boolean isValidZone(String tz) {
		try {
			ZoneId.of(tz);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	private static boolean isValidTimeSlot(String slot) {
		try {
			LocalTime.parse(slot, HH_MM);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}
}
