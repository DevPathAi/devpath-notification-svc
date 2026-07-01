package ai.devpath.notification.inbox;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

	@Autowired NotificationRepository repo;
	@Autowired JdbcTemplate jdbc;

	@Test
	void savesAndChecksExistenceByType() {
		Long userId = jdbc.queryForObject("INSERT INTO users (status) VALUES ('ACTIVE') RETURNING id", Long.class);

		Notification n = new Notification();
		n.setUserId(userId);
		n.setType("WELCOME");
		n.setTitle("환영합니다");
		n.setCreatedAt(Instant.now());
		repo.save(n);

		assertTrue(repo.existsByUserIdAndType(userId, "WELCOME"));
	}
}
