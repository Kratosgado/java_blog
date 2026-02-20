package com.kratosgado.blog.backend.utils;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class OpenApiResponseWrapper implements OperationCustomizer {

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    operation
        .getResponses()
        .forEach(
            (code, response) -> {
              if (isSuccessCode(code)) {
                wrapSuccessResponse(response);
              } else {
                wrapErrorResponse(response);
              }
            });

    return operation;
  }

  private boolean isSuccessCode(String code) {
    return code.startsWith("2");
  }

  private void wrapSuccessResponse(ApiResponse response) {
    Schema<?> dataSchema = extractOriginalSchema(response);

    Schema<?> wrapper =
        new ObjectSchema()
            .addProperty("status", new IntegerSchema().example(200))
            .addProperty("message", new StringSchema().example("Success"))
            .addProperty("data", dataSchema);

    setSchema(response, wrapper);
  }

  private void wrapErrorResponse(ApiResponse response) {
    Schema<?> wrapper =
        new ObjectSchema()
            .addProperty("status", new IntegerSchema().example(400))
            .addProperty("message", new StringSchema().example("Error message"))
            .addProperty("errors", new Schema<>().nullable(true).example(null));

    setSchema(response, wrapper);
  }

  private Schema<?> extractOriginalSchema(ApiResponse response) {
    if (response.getContent() == null) {
      return new ObjectSchema();
    }

    return response.getContent().values().stream()
        .findFirst()
        .map(MediaType::getSchema)
        .orElse(new ObjectSchema());
  }

  private void setSchema(ApiResponse response, Schema<?> schema) {
    Content content = new Content();
    content.addMediaType("application/json", new MediaType().schema(schema));
    response.setContent(content);
  }
}
