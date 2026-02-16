package com.kratosgado.blog.backend.controllers.v1;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.csrf.CsrfToken;

@ExtendWith(MockitoExtension.class)
class CsrfDemoControllerTest {

  @InjectMocks private CsrfDemoController csrfDemoController;

  @Mock private HttpServletRequest request;

  @Mock private CsrfToken csrfToken;

  @Test
  @DisplayName("getCsrfToken should return token details when token present")
  void getCsrfToken_whenPresent_shouldReturnDetails() {
    Mockito.when(request.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);
    Mockito.when(csrfToken.getToken()).thenReturn("tokenValue");
    Mockito.when(csrfToken.getHeaderName()).thenReturn("X-CSRF-TOKEN");
    Mockito.when(csrfToken.getParameterName()).thenReturn("_csrf");

    Map<String, Object> result = csrfDemoController.getCsrfToken(request);

    assertThat(result.get("token")).isEqualTo("tokenValue");
    assertThat(result.get("csrfEnabled")).isEqualTo(true);
  }

  @Test
  @DisplayName("getCsrfToken should indicate disabled when token missing")
  void getCsrfToken_whenMissing_shouldReturnDisabledInfo() {
    Mockito.when(request.getAttribute(CsrfToken.class.getName())).thenReturn(null);

    Map<String, Object> result = csrfDemoController.getCsrfToken(request);

    assertThat(result.get("csrfEnabled")).isEqualTo(false);
    assertThat(result.get("message")).asString().contains("CSRF protection is currently disabled");
  }

  @Test
  @DisplayName("submitForm should echo message and success")
  void submitForm_shouldReturnSuccessPayload() {
    Map<String, Object> result = csrfDemoController.submitForm("hello");

    assertThat(result.get("success")).isEqualTo(true);
    assertThat(result.get("receivedData")).isEqualTo("hello");
  }

  @Test
  @DisplayName("getInfo should contain expected keys")
  void getInfo_shouldReturnInfoMap() {
    Map<String, Object> info = csrfDemoController.getInfo();

    assertThat(info.get("csrfProtection")).isEqualTo("disabled");
    assertThat(info).containsKeys("rationale", "alternativeSecurity", "whenToEnableCsrf");
  }
}
