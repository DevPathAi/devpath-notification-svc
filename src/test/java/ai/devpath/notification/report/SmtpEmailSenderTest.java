package ai.devpath.notification.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class SmtpEmailSenderTest {

    @Test
    void addressBasedSend_setsRecipientSubjectAndBody() {
        JavaMailSender javaMailSender = mock(JavaMailSender.class);
        SmtpEmailSender sender = new SmtpEmailSender(javaMailSender, "no-reply@devpath.ai");

        sender.send("to@x.com", "subj", "body");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertThat(msg.getTo()).containsExactly("to@x.com");
        assertThat(msg.getSubject()).isEqualTo("subj");
        assertThat(msg.getText()).isEqualTo("body");
        assertThat(msg.getFrom()).isEqualTo("no-reply@devpath.ai");
    }
}
