package com.kratosgado.blog.backend.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class SecurityEventListenerTest {

  private final SecurityEventListener listener = new SecurityEventListener();

  @AfterEach
  void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  @DisplayName("onAuthenticationSuccess should log without throwing")
  void onAuthenticationSuccess_shouldHandleEvent() {
    Authentication auth =
        new UsernamePasswordAuthenticationToken("john", null, java.util.List.of());

    mockRequestWithIp("192.168.1.10");

    AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(auth);

    assertThatCode(() -> listener.onAuthenticationSuccess(event)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("onAuthenticationFailure should handle bad credentials and generic failures")
  void onAuthenticationFailure_shouldHandleEvents() {
    Authentication auth =
        new UsernamePasswordAuthenticationToken("john", null, java.util.List.of());
    mockRequestWithIp("10.0.0.5");

    AuthenticationFailureBadCredentialsEvent badCreds =
        new AuthenticationFailureBadCredentialsEvent(auth, new BadCredentialsException("Bad"));

    assertThatCode(() -> listener.onAuthenticationFailure(badCreds))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("onAuthorizationDenied should read request details when available")
  void onAuthorizationDenied_shouldHandleEventWithRequestDetails() {
    Authentication auth =
        new UsernamePasswordAuthenticationToken("alice", null, java.util.List.of());

    mockRequestWithMethodAndUri("GET", "/api/v1/admin/posts");

    AuthorizationDecision decision = new AuthorizationDecision(false);

    @SuppressWarnings("unchecked")
    AuthorizationDeniedEvent<?> event = mock(AuthorizationDeniedEvent.class);
    when(event.getAuthentication()).thenReturn((java.util.function.Supplier) () -> auth);

    assertThatCode(() -> listener.onAuthorizationDenied(event)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("onAuthorizationDenied should handle missing request context")
  void onAuthorizationDenied_shouldHandleMissingRequestContext() {
    Authentication auth =
        new UsernamePasswordAuthenticationToken("alice", null, java.util.List.of());

    RequestContextHolder.resetRequestAttributes();

    AuthorizationDecision decision = new AuthorizationDecision(false);

    @SuppressWarnings("unchecked")
    AuthorizationDeniedEvent<?> event = mock(AuthorizationDeniedEvent.class);
    when(event.getAuthentication()).thenReturn((java.util.function.Supplier) () -> auth);

    assertThatCode(() -> listener.onAuthorizationDenied(event)).doesNotThrowAnyException();
  }

  private void mockRequestWithIp(String ip) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Forwarded-For")).thenReturn(ip);
    when(request.getHeader("X-Real-IP")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn(ip);

    ServletRequestAttributes attrs = new ServletRequestAttributes(request);
    RequestContextHolder.setRequestAttributes(attrs, true);
  }

  private void mockRequestWithMethodAndUri(String method, String uri) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn(method);
    when(request.getRequestURI()).thenReturn(uri);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getHeader("X-Real-IP")).thenReturn("127.0.0.1");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    ServletRequestAttributes attrs = new ServletRequestAttributes(request);
    RequestContextHolder.setRequestAttributes(attrs, true);
  }
}
