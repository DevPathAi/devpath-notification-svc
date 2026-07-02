package ai.devpath.notification.push;

import ai.devpath.notification.inbox.Notification;
import ai.devpath.notification.inbox.NotificationRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;

/** 푸시를 인앱 알림(inbox Notification 행)으로 전달한다. Build 1 inbox 인프라 재사용. */
@Component
public class InboxPushSender implements PushSender {

	private final NotificationRepository notifications;

	public InboxPushSender(NotificationRepository notifications) {
		this.notifications = notifications;
	}

	@Override
	public void send(long userId, String type, String title, String body) {
		Notification n = new Notification();
		n.setUserId(userId);
		n.setType(type);
		n.setTitle(title);
		n.setBody(body);
		n.setCreatedAt(Instant.now());
		notifications.save(n);
	}
}
