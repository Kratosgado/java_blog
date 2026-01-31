package com.kratosgado.blog.backend.config;

import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {
  @Bean
  public GroupedOpenApi v1Api() {
    return GroupedOpenApi.builder()
        .group("v1")
        .pathsToMatch("/v1/**")
        .build();
  }

  @Bean
  public GroupedOpenApi v2Api() {
    return GroupedOpenApi.builder()
        .group("v2")
        .pathsToMatch("/api/v2/**")
        .build();
  }

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
            .addParameters("apiVersion", new Parameter().in(ParameterIn.QUERY.name()).name("version")
                .description("API version (?version=1.0 or ?version=2.0)").required(false)
                .schema(new StringSchema()._default("1.0")
                    .addEnumItem("1.0").addEnumItem("2.0")))
            .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT authentication token")))
        .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
  }
}
