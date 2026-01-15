package com.kratosgado.blog.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

  @Bean
  public OpenAPI blogOpenAPI() {
    return new OpenAPI()
      .info(new Info()
        .title("Blog Backend API")
        .description("Comprehensive REST and GraphQL API for a blogging platform with Spring Boot 3")
        .version("1.0.0")
        .contact(new Contact()
          .name("Blog API Support")
          .email("support@blogapi.com")
          .url("https://blogapi.com"))
        .license(new License()
          .name("Apache 2.0")
          .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
      .servers(List.of(
        new Server().url("http://localhost:8080/api").description("Development Server"),
        new Server().url("https://api.blogapp.com").description("Production Server")))
      .components(new Components()
        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
          .description("JWT authentication token")))
      .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
  }
}
