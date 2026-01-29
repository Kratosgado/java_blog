package com.kratosgado.blog.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = { "com.kratosgado.blog.backend", "com.kratosgado.blog" })
@EnableAsync
@EnableJpaRepositories(basePackages = "com.kratosgado.blog.backend.repositories.jpa")
public class BlogBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(BlogBackendApplication.class, args);
  }
}
