package ai.devpath.notification.beta;

import ai.devpath.notification.report.EmailSender;
import ai.devpath.shared.event.BetaAccessApprovedEvent;
import ai.devpath.shared.event.BetaWaitlistRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class BetaNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(BetaNotificationConsumer.class);
    private final EmailSender email;
    private final JsonMapper jsonMapper;
    private final String notifyEmail;

    public BetaNotificationConsumer(EmailSender email, JsonMapper jsonMapper,
            @Value("${devpath.beta.notify-email:}") String notifyEmail) {
        this.email = email;
        this.jsonMapper = jsonMapper;
        this.notifyEmail = notifyEmail;
    }

    @KafkaListener(topics = "user.beta.waitlisted", groupId = "devpath-notification")
    public void onWaitlisted(String payload) {
        BetaWaitlistRegisteredEvent e;
        try {
            e = jsonMapper.readValue(payload, BetaWaitlistRegisteredEvent.class);
        } catch (Exception ex) {
            log.warn("waitlisted 역직렬화 실패 skip: {}", payload, ex);
            return;
        }
        email.send(e.email(), "DevPath 베타 대기명단에 등록되었습니다",
                "베타 대기명단에 등록되었습니다. 승인되면 이메일로 알려드립니다.");
        if (notifyEmail != null && !notifyEmail.isBlank()) {
            email.send(notifyEmail, "[DevPath] 신규 베타 대기자",
                    "신규 베타 대기자: " + e.email() + " (userId=" + e.userId() + ")");
        }
    }

    @KafkaListener(topics = "user.beta.approved", groupId = "devpath-notification")
    public void onApproved(String payload) {
        BetaAccessApprovedEvent e;
        try {
            e = jsonMapper.readValue(payload, BetaAccessApprovedEvent.class);
        } catch (Exception ex) {
            log.warn("approved 역직렬화 실패 skip: {}", payload, ex);
            return;
        }
        email.send(e.email(), "DevPath 베타 입장이 승인되었습니다",
                "축하합니다! 이제 DevPath 데모에 로그인하실 수 있습니다.");
    }
}
