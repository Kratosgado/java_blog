module com.kratosgado.blog.common {
  // Jakarta Persistence API
  requires jakarta.persistence;
  requires jakarta.validation;

  // Spring Data
  requires spring.data.mongodb;
  requires spring.data.commons;

  // Jackson
  requires com.fasterxml.jackson.annotation;

  // Lombok
  requires static lombok;

  // Swagger/OpenAPI annotations
  requires static io.swagger.v3.oas.annotations;
  requires spring.expression;
  requires spring.data.rest.core;
  requires spring.boot.autoconfigure;
  requires io.swagger.v3.oas.models;

  // Export all packages for use by other modules
  exports com.kratosgado.blog.models;
  exports com.kratosgado.blog.dtos.request;
  exports com.kratosgado.blog.dtos.response;
  exports com.kratosgado.blog.enums;

  // Open for reflection (needed by Hibernate, MongoDB, Gson, etc.)
  opens com.kratosgado.blog.models to
      org.hibernate.orm.core,
      com.google.gson,
      spring.core,
      spring.data.mongodb,
      spring.data.commons;
  opens com.kratosgado.blog.dtos.request to
      io.swagger.v3.oas.annotations,
      com.google.gson;
  opens com.kratosgado.blog.dtos.response to
      io.swagger.v3.oas.annotations,
      spring.boot.autoconfigure,
      io.swagger.v3.oas.models,
      spring.data.rest.core,
      com.google.gson;
  opens com.kratosgado.blog.enums to
      com.google.gson;
}
