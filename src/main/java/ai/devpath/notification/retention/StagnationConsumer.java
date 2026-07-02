package ai.devpath.notification.retention;

import ai.devpath.notification.push.PushSender;
import ai.devpath.shared.event.UserStagnatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** progress.user.stagnated 구독 → ai-svc 재참여 문구(실패 시 폴백) → PushSender 전달. */
@Component
public class StagnationConsumer {

	private static final Logger log = LoggerFactory.getLogger(StagnationConsumer.class);
	private static final String TYPE = "RE_ENGAGEMENT";
	private static final String TITLE = "다시 시작해볼까요?";
	private static final String FALLBACK = "오랜만이에요! 다시 학습을 시작해볼까요? 오늘 한 걸음이면 충분해요.";

	private final ReEngagementClient reEngagement;
	private final PushSender pushSender;
	private final JsonMapper jsonMapper;

	public StagnationConsumer(ReEngagementClient reEngagement, PushSender pushSender, JsonMapper jsonMapper) {
		this.reEngagement = reEngagement;
		this.pushSender = pushSender;
		this.jsonMapper = jsonMapper;
	}

	@KafkaListener(topics = UserStagnatedEvent.EVENT_TYPE, groupId = "devpath-notification")
	public void onUserStagnated(String payload) {
		UserStagnatedEvent event;
		try {
			event = jsonMapper.readValue(payload, UserStagnatedEvent.class);
		} catch (Exception e) {
			log.warn("UserStagnatedEvent 역직렬화 실패 — skip: {}", payload, e);
			return; // poison 무한재시도 방지
		}
		String message;
		try {
			message = reEngagement.suggest(event.userId(), event.lastActiveAt(),
					event.daysInactive(), event.currentLearningPathSummary());
		} catch (ReEngagementUnavailableException e) {
			log.warn("ai-svc 재참여 문구 실패 — 폴백 문구 사용 userId={}", event.userId(), e);
			message = FALLBACK; // 그레이스풀 디그레이드(스펙 §에러 처리)
		}
		pushSender.send(event.userId(), TYPE, TITLE, message);
	}
}
