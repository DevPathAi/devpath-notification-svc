package ai.devpath.notification.beta;

import static org.mockito.Mockito.*;

import ai.devpath.notification.report.EmailSender;
import ai.devpath.shared.event.BetaAccessApprovedEvent;
import ai.devpath.shared.event.BetaWaitlistRegisteredEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class BetaNotificationConsumerTest {

    private final EmailSender email = mock(EmailSender.class);
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final BetaNotificationConsumer consumer =
            new BetaNotificationConsumer(email, mapper, "admin@devpath.ai");

    @Test
    void waitlisted_emailsApplicantAndAdmin() {
        var e = new BetaWaitlistRegisteredEvent(UUID.randomUUID(), Instant.now(), 1L, "u@x.com");
        consumer.onWaitlisted(mapper.writeValueAsString(e));
        verify(email).send(eq("u@x.com"), contains("대기"), anyString());
        verify(email).send(eq("admin@devpath.ai"), contains("신규"), anyString());
    }

    @Test
    void approved_emailsApplicant() {
        var e = new BetaAccessApprovedEvent(UUID.randomUUID(), Instant.now(), 1L, "u@x.com");
        consumer.onApproved(mapper.writeValueAsString(e));
        verify(email).send(eq("u@x.com"), contains("입장"), anyString());
    }

    @Test
    void malformed_isSkipped() {
        consumer.onWaitlisted("{ not json");
        verifyNoInteractions(email);
    }
}
