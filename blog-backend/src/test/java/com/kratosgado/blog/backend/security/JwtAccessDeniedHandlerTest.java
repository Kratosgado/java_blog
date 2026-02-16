package com.kratosgado.blog.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kratosgado.blog.dtos.response.ResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class JwtAccessDeniedHandlerTest {

  @InjectMocks
  private JwtAccessDeniedHandler handler;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private AccessDeniedException accessDeniedException;

  @Test
  @DisplayName("handle should write JSON error response with 403 status")
  void handle_shouldWriteJsonError() throws IOException, ServletException {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    org.mockito.Mockito.when(request.getMethod()).thenReturn("GET");
    org.mockito.Mockito.when(request.getRequestURI()).thenReturn("/protected");
    org.mockito.Mockito.when(accessDeniedException.getMessage()).thenReturn("denied");
    org.mockito.Mockito.when(response.getWriter()).thenReturn(pw);

    handler.handle(request, response, accessDeniedException);

    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    verify(response).setStatus(HttpStatus.FORBIDDEN.value());

    pw.flush();
    String json = sw.toString();
    ObjectMapper mapper = new ObjectMapper();
    ResponseDto<?> dto = mapper.readValue(json, ResponseDto.class);

    assertThat(dto.status()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(dto.message()).contains("Access denied");
  }
}
