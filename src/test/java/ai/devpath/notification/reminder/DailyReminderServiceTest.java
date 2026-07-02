package ai.devpath.notification.reminder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.devpath.notification.inbox.NotificationRepository;
import ai.devpath.notification.prefs.UserNotificationPrefs;
import ai.devpath.notification.prefs.UserNotificationPrefsRepository;
import ai.devpath.notification.push.PushSender;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** 순수 단위테스트(DB·Spring 불요). TZ 환산·당일 중복방지 판정 로직. */
class DailyReminderServiceTest {

  private UserNotificationPrefsRepository prefs;
  private NotificationRepository inbox;
  private PushSender pushSender;
  private DailyReminderService service;

  @BeforeEach
  void setUp() {
    prefs = mock(UserNotificationPrefsRepository.class);
    inbox = mock(NotificationRepository.class);
    pushSender = mock(PushSender.class);
    service = new DailyReminderService(prefs, inbox, pushSender);
  }

  private UserNotificationPrefs prefsRow(long userId, String tz, String slot) {
    UserNotificationPrefs p = new UserNotificationPrefs();
    p.setUserId(userId);
    p.setTimezone(tz);
    p.setPreferredTimeSlot(slot);
    p.setReminderEnabled(true);
    return p;
  }

  /** 주어진 zone에서 로컬 시각이 hh:mm이 되도록 하는 Instant. */
  private Instant instantAtLocal(String tz, int hour, int minute) {
    ZoneId zone = ZoneId.of(tz);
    ZonedDateTime local = LocalDate.of(2026, 7, 2).atTime(LocalTime.of(hour, minute)).atZone(zone);
    return local.toInstant();
  }

  @Test
  void sendsWhenLocalTimeReachedAndNotYetSent() {
    when(prefs.findByReminderEnabledTrue()).thenReturn(List.of(prefsRow(1L, "Asia/Seoul", "19:00")));
    when(inbox.existsByUserIdAndTypeAndCreatedAtGreaterThanEqual(eq(1L), eq("DAILY_REMINDER"), any()))
        .thenReturn(false);

    service.sendDueReminders(instantAtLocal("Asia/Seoul", 19, 5)); // 로컬 19:05

    verify(pushSender).send(eq(1L), eq("DAILY_REMINDER"), anyString(), anyString());
  }

  @Test
  void doesNotSendBeforePreferredTime() {
    when(prefs.findByReminderEnabledTrue()).thenReturn(List.of(prefsRow(1L, "Asia/Seoul", "19:00")));
    when(inbox.existsByUserIdAndTypeAndCreatedAtGreaterThanEqual(anyLong(), anyString(), any()))
        .thenReturn(false);

    service.sendDueReminders(instantAtLocal("Asia/Seoul", 18, 55)); // 로컬 18:55

    verify(pushSender, never()).send(anyLong(), anyString(), anyString(), anyString());
  }

  @Test
  void doesNotSendTwiceSameDay() {
    when(prefs.findByReminderEnabledTrue()).thenReturn(List.of(prefsRow(1L, "Asia/Seoul", "19:00")));
    when(inbox.existsByUserIdAndTypeAndCreatedAtGreaterThanEqual(eq(1L), eq("DAILY_REMINDER"), any()))
        .thenReturn(true); // 오늘 이미 보냄

    service.sendDueReminders(instantAtLocal("Asia/Seoul", 19, 5));

    verify(pushSender, never()).send(anyLong(), anyString(), anyString(), anyString());
  }

  @Test
  void respectsPerUserTimezone() {
    // 같은 Instant라도 뉴욕 로컬은 아직 선호시간 전, 서울 로컬은 도달
    when(prefs.findByReminderEnabledTrue()).thenReturn(List.of(
        prefsRow(1L, "Asia/Seoul", "19:00"),
        prefsRow(2L, "America/New_York", "19:00")));
    when(inbox.existsByUserIdAndTypeAndCreatedAtGreaterThanEqual(anyLong(), anyString(), any()))
        .thenReturn(false);

    service.sendDueReminders(instantAtLocal("Asia/Seoul", 19, 5)); // 서울 19:05 = 뉴욕 06:05

    verify(pushSender).send(eq(1L), anyString(), anyString(), anyString());
    verify(pushSender, never()).send(eq(2L), anyString(), anyString(), anyString());
  }

  @Test
  void skipsRowWithInvalidTimezoneWithoutFailingBatch() {
    when(prefs.findByReminderEnabledTrue()).thenReturn(List.of(
        prefsRow(1L, "Mars/Olympus", "19:00"), // 잘못된 tz — skip
        prefsRow(2L, "Asia/Seoul", "19:00")));
    when(inbox.existsByUserIdAndTypeAndCreatedAtGreaterThanEqual(anyLong(), anyString(), any()))
        .thenReturn(false);

    service.sendDueReminders(instantAtLocal("Asia/Seoul", 19, 5));

    verify(pushSender).send(eq(2L), anyString(), anyString(), anyString());
    verify(pushSender, never()).send(eq(1L), anyString(), anyString(), anyString());
  }
}
