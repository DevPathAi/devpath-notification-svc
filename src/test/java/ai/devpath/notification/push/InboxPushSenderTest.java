package ai.devpath.notification.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ai.devpath.notification.inbox.Notification;
import ai.devpath.notification.inbox.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** 순수 단위테스트(DB 불요). 푸시=inbox Notification 행 생성인지 검증. */
class InboxPushSenderTest {

  @Test
  void sendPersistsInboxNotificationRow() {
    NotificationRepository repo = mock(NotificationRepository.class);
    InboxPushSender sender = new InboxPushSender(repo);

    sender.send(555L, "DAILY_REMINDER", "제목", "본문");

    ArgumentCaptor<Notification> cap = ArgumentCaptor.forClass(Notification.class);
    verify(repo).save(cap.capture());
    Notification n = cap.getValue();
    assertThat(n.getUserId()).isEqualTo(555L);
    assertThat(n.getType()).isEqualTo("DAILY_REMINDER");
    assertThat(n.getTitle()).isEqualTo("제목");
    assertThat(n.getBody()).isEqualTo("본문");
    assertThat(n.getCreatedAt()).isNotNull();
  }
}
