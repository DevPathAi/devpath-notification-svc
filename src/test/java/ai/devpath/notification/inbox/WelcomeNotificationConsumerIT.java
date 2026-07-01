package ai.devpath.notification.inbox;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ai.devpath.shared.event.UserRegisteredEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;

/**
 * 끝단간 통합 테스트: 실제 Kafka(embedded) 토픽에 UserRegisteredEvent를 직접 발행하면
 * WelcomeNotificationConsumer(@KafkaListener)가 실제로 구독해 notifications에 WELCOME 행을
 * 쓰는지 검증한다. platform-svc의 outbox→Kafka 발행측 검증은 platform-svc의
 * EventPropagationIT가 담당하므로(이 서비스는 소비측만 책임), 이 테스트는 발행 과정을
 * KafkaTemplate로 직접 흉내낸다.
 */
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {UserRegisteredEvent.EVENT_TYPE},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class WelcomeNotificationConsumerIT {

    @Autowired JdbcTemplate jdbc;
    @Autowired NotificationRepository notifications;
    @Autowired JsonMapper jsonMapper;
    @Autowired KafkaTemplate<String, String> kafka;

    @Test
    void realKafkaMessage_isConsumedIntoWelcomeNotification() throws Exception {
        Long userId = jdbc.queryForObject("INSERT INTO users (status) VALUES ('ACTIVE') RETURNING id", Long.class);

        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID(), Instant.now(), userId, "GITHUB", "it-" + System.nanoTime() + "@example.com");
        String payload = jsonMapper.writeValueAsString(event);

        kafka.send(UserRegisteredEvent.EVENT_TYPE, String.valueOf(userId), payload).get();

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    long count = notifications.findAll().stream()
                            .filter(n -> n.getUserId().equals(userId) && "WELCOME".equals(n.getType()))
                            .count();
                    assertEquals(1L, count,
                            "notifications 테이블에 userId=" + userId + " WELCOME 행이 정확히 1개여야 한다");
                });
    }
}
