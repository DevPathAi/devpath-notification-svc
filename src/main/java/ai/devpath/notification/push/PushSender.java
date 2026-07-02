package ai.devpath.notification.push;

/**
 * 사용자에게 알림을 "전달"하는 채널 추상화(스펙의 EmailSender 패턴과 동일 철학).
 * 이번 빌드의 구현은 인앱 inbox 저장({@link InboxPushSender}). 실제 FCM 발송은 후속.
 */
public interface PushSender {
	void send(long userId, String type, String title, String body);
}
