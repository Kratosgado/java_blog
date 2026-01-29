
package com.kratosgado.blog.backend.utils;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.kratosgado.blog.dtos.response.ResponseDto;

@ControllerAdvice
public class ResponseWrapper implements ResponseBodyAdvice<Object> {
  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {

    int status = 200;
    if (response instanceof ServletServerHttpResponse servletResponse) {
      status = servletResponse.getServletResponse().getStatus();
    }

    // Handle string responses specially (they need manual conversion)
    if (body instanceof String) {
      return ResponseDto.success(status, (String) body, null);
    }

    // Skip if already wrapped
    if (body instanceof ResponseDto) {
      return body;
    }

    return ResponseDto.success(status, "Operation completed successfully", body);
  }

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attr != null) {
      String path = attr.getRequest().getServletPath();
      if (path.startsWith("/docs") || path.startsWith("/v3/api-docs"))
        return false;
    }

    Class<?> parameterType = returnType.getParameterType();
    return !parameterType.equals(ResponseDto.class)
        && !parameterType.equals(ResponseEntity.class);
  }
}
