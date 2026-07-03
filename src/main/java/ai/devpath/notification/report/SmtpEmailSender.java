package ai.devpath.notification.report;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "devpath.mail.provider", havingValue = "smtp")
public class SmtpEmailSender implements EmailSender {

	private final JavaMailSender mailSender;
	private final String from;

	public SmtpEmailSender(JavaMailSender mailSender,
			@Value("${devpath.mail.from-address:no-reply@devpath.ai}") String from) {
		this.mailSender = mailSender;
		this.from = from;
	}

	@Override
	public void send(long userId, String subject, String body) {
		// 실제 수신자 이메일은 platform-svc 소관 — 후속 연동. 현재는 발신/본문 구성까지.
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom(from);
		msg.setSubject(subject);
		msg.setText(body);
		// msg.setTo(...) 는 수신자 조회 API(platform-svc) 연동 후속
		mailSender.send(msg);
	}
}
