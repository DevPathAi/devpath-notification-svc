package ai.devpath.notification.report;

/** 이메일 전달 추상화(스펙의 EmailSender 패턴). 기본 구현은 MockEmailSender, 운영은 SmtpEmailSender. */
public interface EmailSender {
	void send(long userId, String subject, String body);
	void send(String toEmail, String subject, String body);
}
