package ai.devpath.notification.retention;

import java.time.Instant;

public interface ReEngagementClient {
	/** ai-svc에 재참여 문구 생성 요청. 실패 시 ReEngagementUnavailableException. */
	String suggest(long userId, Instant lastActiveAt, int daysInactive, String currentLearningPathSummary);
}
