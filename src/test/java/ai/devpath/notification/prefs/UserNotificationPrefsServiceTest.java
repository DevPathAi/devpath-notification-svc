package ai.devpath.notification.prefs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** 순수 단위테스트(Spring 컨텍스트·DB 불요). prefs 조회 기본값 + upsert 로직. */
class UserNotificationPrefsServiceTest {

  private UserNotificationPrefsRepository repo;
  private UserNotificationPrefsService service;

  @BeforeEach
  void setUp() {
    repo = mock(UserNotificationPrefsRepository.class);
    service = new UserNotificationPrefsService(repo);
  }

  @Test
  void getOrDefaultReturnsDefaultsWhenNoRow() {
    when(repo.findById(42L)).thenReturn(Optional.empty());

    UserNotificationPrefs p = service.getOrDefault(42L);

    assertThat(p.getUserId()).isEqualTo(42L);
    assertThat(p.getTimezone()).isEqualTo("Asia/Seoul");
    assertThat(p.getPreferredTimeSlot()).isEqualTo("19:00");
    assertThat(p.getReminderEnabled()).isTrue();
    assertThat(p.getWeeklyReportEmailEnabled()).isTrue();
  }

  @Test
  void getOrDefaultReturnsStoredRowWhenPresent() {
    UserNotificationPrefs existing = new UserNotificationPrefs();
    existing.setUserId(42L);
    existing.setTimezone("America/New_York");
    when(repo.findById(42L)).thenReturn(Optional.of(existing));

    assertThat(service.getOrDefault(42L).getTimezone()).isEqualTo("America/New_York");
  }

  @Test
  void upsertCreatesRowWithAllFieldsAndUpdatedAt() {
    when(repo.findById(42L)).thenReturn(Optional.empty());
    when(repo.save(any(UserNotificationPrefs.class))).thenAnswer(inv -> inv.getArgument(0));

    service.upsert(42L, "Europe/Paris", "08:30", false, false);

    ArgumentCaptor<UserNotificationPrefs> cap = ArgumentCaptor.forClass(UserNotificationPrefs.class);
    verify(repo).save(cap.capture());
    UserNotificationPrefs saved = cap.getValue();
    assertThat(saved.getUserId()).isEqualTo(42L);
    assertThat(saved.getTimezone()).isEqualTo("Europe/Paris");
    assertThat(saved.getPreferredTimeSlot()).isEqualTo("08:30");
    assertThat(saved.getReminderEnabled()).isFalse();
    assertThat(saved.getWeeklyReportEmailEnabled()).isFalse();
    assertThat(saved.getUpdatedAt()).isNotNull();
  }

  @Test
  void upsertUpdatesExistingRowInPlace() {
    UserNotificationPrefs existing = new UserNotificationPrefs();
    existing.setUserId(42L);
    existing.setTimezone("Asia/Seoul");
    when(repo.findById(42L)).thenReturn(Optional.of(existing));
    when(repo.save(any(UserNotificationPrefs.class))).thenAnswer(inv -> inv.getArgument(0));

    service.upsert(42L, "America/New_York", "21:00", true, false);

    verify(repo).save(existing); // 같은 행 재사용
    assertThat(existing.getTimezone()).isEqualTo("America/New_York");
    assertThat(existing.getPreferredTimeSlot()).isEqualTo("21:00");
  }
}
