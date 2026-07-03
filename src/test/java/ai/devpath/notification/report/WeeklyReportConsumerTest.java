package ai.devpath.notification.report;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.devpath.notification.prefs.UserNotificationPrefs;
import ai.devpath.notification.prefs.UserNotificationPrefsRepository;
import ai.devpath.notification.push.PushSender;
import ai.devpath.shared.event.WeeklyReportGeneratedEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class WeeklyReportConsumerTest {

  private final JsonMapper jsonMapper = new JsonMapper();

  private String payload(long userId) {
    return jsonMapper.writeValueAsString(new WeeklyReportGeneratedEvent(
        UUID.randomUUID(), Instant.now(), userId, LocalDate.of(2026, 7, 5), 12, 75, List.of("학생"), "Spring 실습"));
  }

  private UserNotificationPrefs prefs(long userId, boolean emailOn) {
    UserNotificationPrefs p = new UserNotificationPrefs();
    p.setUserId(userId); p.setWeeklyReportEmailEnabled(emailOn);
    return p;
  }

  @Test
  void savesReportSendsEmailAndPushWhenEmailEnabled() {
    WeeklyReportRepository reports = mock(WeeklyReportRepository.class);
    when(reports.existsByUserIdAndWeekOf(eq(500L), any())).thenReturn(false);
    UserNotificationPrefsRepository prefs = mock(UserNotificationPrefsRepository.class);
    when(prefs.findById(500L)).thenReturn(Optional.of(prefs(500L, true)));
    EmailSender email = mock(EmailSender.class);
    PushSender push = mock(PushSender.class);
    var c = new WeeklyReportConsumer(reports, prefs, email, push, jsonMapper);

    c.onWeeklyReport(payload(500L));

    verify(reports).save(any(WeeklyReport.class));
    verify(email).send(eq(500L), anyString(), anyString());
    verify(push).send(eq(500L), eq("WEEKLY_REPORT"), anyString(), anyString());
  }

  @Test
  void skipsEmailWhenDisabledButStillPushes() {
    WeeklyReportRepository reports = mock(WeeklyReportRepository.class);
    when(reports.existsByUserIdAndWeekOf(anyLong(), any())).thenReturn(false);
    UserNotificationPrefsRepository prefs = mock(UserNotificationPrefsRepository.class);
    when(prefs.findById(501L)).thenReturn(Optional.of(prefs(501L, false)));
    EmailSender email = mock(EmailSender.class);
    PushSender push = mock(PushSender.class);
    var c = new WeeklyReportConsumer(reports, prefs, email, push, jsonMapper);

    c.onWeeklyReport(payload(501L));

    verify(email, never()).send(anyLong(), anyString(), anyString());
    verify(push).send(eq(501L), eq("WEEKLY_REPORT"), anyString(), anyString());
  }

  @Test
  void skipsDuplicateWeek() {
    WeeklyReportRepository reports = mock(WeeklyReportRepository.class);
    when(reports.existsByUserIdAndWeekOf(eq(502L), any())).thenReturn(true); // 이미 처리됨
    var c = new WeeklyReportConsumer(reports, mock(UserNotificationPrefsRepository.class),
        mock(EmailSender.class), mock(PushSender.class), jsonMapper);

    c.onWeeklyReport(payload(502L));

    verify(reports, never()).save(any());
  }

  @Test
  void skipsPoisonPayload() {
    var c = new WeeklyReportConsumer(mock(WeeklyReportRepository.class), mock(UserNotificationPrefsRepository.class),
        mock(EmailSender.class), mock(PushSender.class), jsonMapper);
    c.onWeeklyReport("{ not json"); // 예외 없이 skip
  }
}
