package ai.devpath.notification.inbox;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import tools.jackson.databind.json.JsonMapper;
import ai.devpath.shared.event.UserRegisteredEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WelcomeNotificationConsumerTest {

	@Autowired WelcomeNotificationConsumer consumer;
	@Autowired NotificationRepository notifications;
	@Autowired JdbcTemplate jdbc;
	@Autowired JsonMapper om;

	@Test
	void createsWelcomeNotificationOnceIdempotently() throws Exception {
		Long userId = jdbc.queryForObject("INSERT INTO users (status) VALUES ('ACTIVE') RETURNING id", Long.class);
		String payload = om.writeValueAsString(
				new UserRegisteredEvent(UUID.randomUUID(), Instant.now(), userId, "GITHUB", "w" + System.nanoTime() + "@example.com"));

		consumer.onUserRegistered(payload);
		consumer.onUserRegistered(payload); // 중복

		assertEquals(1, notifications.findAll().stream()
				.filter(n -> n.getUserId().equals(userId) && n.getType().equals("WELCOME")).count());
	}

	@Test
	void poisonPayloadIsSkippedWithoutThrowing() {
		// 역직렬화 불가 payload는 예외 없이 skip(다른 소비자와 동일, Kafka 무한재시도 방지).
		assertDoesNotThrow(() -> consumer.onUserRegistered("{ not-json"));
	}
}
