module com.kratosgado.blog.common {
  requires jakarta.validation;

  // Jackson
  requires com.fasterxml.jackson.annotation;

  // Lombok
  requires static lombok;

  // Swagger/OpenAPI annotations
  requires static io.swagger.v3.oas.annotations;

  // Export all packages for use by other modules
  exports com.kratosgado.blog.models;
  exports com.kratosgado.blog.dtos.request;
  exports com.kratosgado.blog.dtos.response;
  exports com.kratosgado.blog.enums;

  // Open for reflection (needed by Gson, etc.)
  opens com.kratosgado.blog.models to
      com.google.gson;
  opens com.kratosgado.blog.dtos.request to
      io.swagger.v3.oas.annotations,
      com.google.gson;
  opens com.kratosgado.blog.dtos.response to
      io.swagger.v3.oas.annotations,
      com.google.gson;
  opens com.kratosgado.blog.enums to
      com.google.gson;
}
