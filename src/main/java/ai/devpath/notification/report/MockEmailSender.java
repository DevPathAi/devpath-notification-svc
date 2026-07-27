package ai.devpath.notification.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "devpath.mail.provider", havingValue = "mock", matchIfMissing = true)
public class MockEmailSender implements EmailSender {
	private static final Logger log = LoggerFactory.getLogger(MockEmailSender.class);

	@Override
	public void send(long userId, String subject, String body) {
		log.info("[MockEmailSender] userId={} subject={} (실제 발송 안 함)", userId, subject);
	}

	@Override
	public void send(String toEmail, String subject, String body) {
		log.info("[MockEmailSender] to={} subject={} (실제 발송 안 함)", toEmail, subject);
	}
}
