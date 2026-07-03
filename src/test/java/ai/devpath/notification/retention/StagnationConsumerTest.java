package ai.devpath.notification.retention;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.devpath.notification.push.PushSender;
import ai.devpath.shared.event.UserStagnatedEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** 순수 단위테스트(Spring/DB 불요). 정체 이벤트 → ai 문구/폴백 → PushSender 전달. */
class StagnationConsumerTest {

  private final JsonMapper jsonMapper = new JsonMapper();

  private String payload(long userId) {
    return jsonMapper.writeValueAsString(new UserStagnatedEvent(
        UUID.randomUUID(), Instant.now(), userId, Instant.now().minusSeconds(3 * 86400), 3, "백엔드 스프링 트랙 (12주 과정)"));
  }

  @Test
  void usesAiMessageWhenAvailable() {
    ReEngagementClient ai = mock(ReEngagementClient.class);
    when(ai.suggest(anyLong(), any(), anyInt(), any())).thenReturn("AI 맞춤 문구");
    PushSender push = mock(PushSender.class);
    StagnationConsumer c = new StagnationConsumer(ai, push, jsonMapper);

    c.onUserStagnated(payload(555L));

    verify(push).send(eq(555L), eq("RE_ENGAGEMENT"), any(), eq("AI 맞춤 문구"));
  }

  @Test
  void fallsBackWhenAiFails() {
    ReEngagementClient ai = mock(ReEngagementClient.class);
    when(ai.suggest(anyLong(), any(), anyInt(), any()))
        .thenThrow(new ReEngagementUnavailableException("down", null));
    PushSender push = mock(PushSender.class);
    StagnationConsumer c = new StagnationConsumer(ai, push, jsonMapper);

    c.onUserStagnated(payload(556L));

    // 폴백 문구로라도 반드시 발송(전체 알림이 막히면 안 됨 — 스펙 §에러 처리)
    verify(push).send(eq(556L), eq("RE_ENGAGEMENT"), any(), any());
  }

  @Test
  void skipsPoisonPayloadWithoutThrowing() {
    StagnationConsumer c = new StagnationConsumer(mock(ReEngagementClient.class), mock(PushSender.class), jsonMapper);
    c.onUserStagnated("{ not valid json"); // 예외 없이 skip
  }
}
