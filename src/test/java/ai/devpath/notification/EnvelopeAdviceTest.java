package ai.devpath.notification;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.devpath.shared.error.ApiException;
import ai.devpath.shared.error.ApiExceptionHandler;
import ai.devpath.shared.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 정합성 3차 M-18: notification-svc의 공용 {@link ApiExceptionHandler} 채택 회귀 검증.
 *
 * <p>{@link ApiException}이 스펙 §3.4 중첩 envelope({@code {"error":{"code","message",...}}})로
 * 렌더되는지 확인한다. notification 컨트롤러가 향후 도메인 예외를 던지거나 프레임워크 예외가
 * 발생할 때 shared 표준 envelope로 통일됨을 보장한다(이전에는 전역 핸들러 부재).
 */
@WebMvcTest(controllers = EnvelopeAdviceTest.BoomController.class)
@Import({ApiExceptionHandler.class, EnvelopeAdviceTest.BoomController.class})
@AutoConfigureMockMvc(addFilters = false)
class EnvelopeAdviceTest {

  @Autowired MockMvc mvc;

  @Test
  void apiExceptionRendersSpecEnvelope() throws Exception {
    mvc.perform(get("/__test/boom"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code").value("FORBIDDEN"))
        .andExpect(jsonPath("$.error.message").value("boom"));
  }

  /** 테스트 전용 스텁 — main 코드에 영향 없음. */
  @RestController
  static class BoomController {
    @GetMapping("/__test/boom")
    public void boom() {
      throw new ApiException(ErrorCode.FORBIDDEN, "boom");
    }
  }
}
