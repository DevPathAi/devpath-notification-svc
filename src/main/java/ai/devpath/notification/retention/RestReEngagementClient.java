package ai.devpath.notification.retention;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestReEngagementClient implements ReEngagementClient {

	private final RestClient restClient;

	public RestReEngagementClient(
			@Value("${devpath.ai-svc.base-url:http://localhost:8084}") String baseUrl,
			@Value("${devpath.ai-svc.timeout:PT5S}") Duration timeout) {
		var factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(timeout);
		factory.setReadTimeout(timeout);
		this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
	}

	@Override
	public String suggest(long userId, Instant lastActiveAt, int daysInactive, String currentLearningPathSummary) {
		try {
			ReEngagementResponse res = restClient.post()
					.uri("/ai/re-engagement")
					.body(Map.of(
							"userId", userId,
							"lastActiveAt", lastActiveAt.toString(),
							"daysInactive", daysInactive,
							"currentLearningPathSummary", currentLearningPathSummary == null ? "" : currentLearningPathSummary))
					.retrieve()
					.body(ReEngagementResponse.class);
			if (res == null || res.message() == null || res.message().isBlank()) {
				throw new ReEngagementUnavailableException("ai-svc 응답이 비어 있음", null);
			}
			return res.message();
		} catch (RestClientException e) {
			throw new ReEngagementUnavailableException("ai-svc re-engagement 호출 실패", e);
		}
	}

	record ReEngagementResponse(String message) {}
}
