module com.kratosgado.blog.common {
  // Jakarta Persistence API
  requires jakarta.persistence;
  requires jakarta.validation;

  // Spring Data MongoDB
  requires spring.data.commons;
  requires spring.data.mongodb;

  // Lombok
  requires static lombok;

  // Export all packages for use by other modules
  exports com.kratosgado.blog.models;
  exports com.kratosgado.blog.dtos.request;
  exports com.kratosgado.blog.dtos.response;

  // Open for reflection (needed by Hibernate, MongoDB, Gson, etc.)
  opens com.kratosgado.blog.models to org.hibernate.orm.core, com.google.gson, spring.core, spring.data.mongodb;
  opens com.kratosgado.blog.dtos.request to com.google.gson;
  opens com.kratosgado.blog.dtos.response to com.google.gson;
}
