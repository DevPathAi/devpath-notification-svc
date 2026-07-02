package ai.devpath.notification.prefs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InternalPrefsControllerIT {

  @Autowired MockMvc mvc;
  @Autowired UserNotificationPrefsRepository prefs;

  @Test
  void returnsDefaultTimezoneWhenNoRowExists() throws Exception {
    mvc.perform(get("/notifications/internal/prefs/timezones").param("userIds", "999001"))
        .andExpect(status().isOk())
        .andExpect(content().json("[{\"userId\":999001,\"timezone\":\"Asia/Seoul\"}]"));
  }

  @Test
  void returnsStoredTimezoneWhenRowExists() throws Exception {
    UserNotificationPrefs row = new UserNotificationPrefs();
    row.setUserId(999002L);
    row.setTimezone("America/New_York");
    prefs.save(row);

    mvc.perform(get("/notifications/internal/prefs/timezones").param("userIds", "999002"))
        .andExpect(status().isOk())
        .andExpect(content().json("[{\"userId\":999002,\"timezone\":\"America/New_York\"}]"));
  }
}
