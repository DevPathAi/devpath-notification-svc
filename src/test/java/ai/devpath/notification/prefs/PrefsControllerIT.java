package ai.devpath.notification.prefs;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrefsControllerIT {

  @Autowired MockMvc mvc;

  private static Jwt jwtFor(long userId) {
    return Jwt.withTokenValue("t").header("alg", "none").subject(String.valueOf(userId)).build();
  }

  @Test
  void getReturnsDefaultsWhenNoRow() throws Exception {
    mvc.perform(get("/notifications/prefs/me").with(jwt().jwt(jwtFor(990101L))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.timezone").value("Asia/Seoul"))
        .andExpect(jsonPath("$.preferredTimeSlot").value("19:00"))
        .andExpect(jsonPath("$.reminderEnabled").value(true))
        .andExpect(jsonPath("$.weeklyReportEmailEnabled").value(true));
  }

  @Test
  void putThenGetReturnsUpdatedValues() throws Exception {
    mvc.perform(put("/notifications/prefs/me").with(jwt().jwt(jwtFor(990102L)))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"timezone\":\"America/New_York\",\"preferredTimeSlot\":\"08:30\",\"reminderEnabled\":false,\"weeklyReportEmailEnabled\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.timezone").value("America/New_York"))
        .andExpect(jsonPath("$.preferredTimeSlot").value("08:30"))
        .andExpect(jsonPath("$.reminderEnabled").value(false));

    mvc.perform(get("/notifications/prefs/me").with(jwt().jwt(jwtFor(990102L))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.timezone").value("America/New_York"))
        .andExpect(jsonPath("$.preferredTimeSlot").value("08:30"));
  }

  @Test
  void putRejectsInvalidTimezone() throws Exception {
    mvc.perform(put("/notifications/prefs/me").with(jwt().jwt(jwtFor(990103L)))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"timezone\":\"Mars/Olympus\",\"preferredTimeSlot\":\"19:00\",\"reminderEnabled\":true,\"weeklyReportEmailEnabled\":true}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void putRejectsInvalidTimeSlot() throws Exception {
    mvc.perform(put("/notifications/prefs/me").with(jwt().jwt(jwtFor(990104L)))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"timezone\":\"Asia/Seoul\",\"preferredTimeSlot\":\"25:99\",\"reminderEnabled\":true,\"weeklyReportEmailEnabled\":true}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getWithoutJwtIsUnauthorized() throws Exception {
    mvc.perform(get("/notifications/prefs/me"))
        .andExpect(status().isUnauthorized());
  }
}
