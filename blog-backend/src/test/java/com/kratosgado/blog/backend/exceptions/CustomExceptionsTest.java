package com.kratosgado.blog.backend.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomExceptionsTest {

  @Test
  @DisplayName("ForbiddenException should use default and custom messages")
  void forbiddenException_messages() {
    ForbiddenException exDefault = new ForbiddenException();
    ForbiddenException exCustom = new ForbiddenException("msg");

    assertThat(exDefault.getMessage()).contains("Access denied");
    assertThat(exCustom.getMessage()).isEqualTo("msg");
  }

  @Test
  @DisplayName("UnauthorizedException should use default and custom messages")
  void unauthorizedException_messages() {
    UnauthorizedException exDefault = new UnauthorizedException();
    UnauthorizedException exCustom = new UnauthorizedException("msg");

    assertThat(exDefault.getMessage()).contains("Authentication required");
    assertThat(exCustom.getMessage()).isEqualTo("msg");
  }

  @Test
  @DisplayName("InvalidRequestException should support message and cause")
  void invalidRequestException_messages() {
    InvalidRequestException exMessage = new InvalidRequestException("msg");
    Throwable cause = new IllegalArgumentException("bad");
    InvalidRequestException exWithCause = new InvalidRequestException("msg2", cause);

    assertThat(exMessage.getMessage()).isEqualTo("msg");
    assertThat(exWithCause.getMessage()).isEqualTo("msg2");
    assertThat(exWithCause.getCause()).isSameAs(cause);
  }

  @Test
  @DisplayName("ResourceNotFoundException should format messages correctly")
  void resourceNotFoundException_messages() {
    ResourceNotFoundException exSimple = new ResourceNotFoundException("Not found");
    ResourceNotFoundException exField =
        new ResourceNotFoundException("Post", "id", 1L);
    ResourceNotFoundException exId = new ResourceNotFoundException("Post", 2L);

    assertThat(exSimple.getMessage()).isEqualTo("Not found");
    assertThat(exField.getMessage()).contains("Post not found with id: '1'");
    assertThat(exId.getMessage()).contains("Post not found with id: '2'");
  }
}
