package com.kratosgado.blog.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = { "com.kratosgado.blog.backend", "com.kratosgado.blog" })
@EntityScan(basePackages = "com.kratosgado.blog.models")
@EnableJpaRepositories(basePackages = "com.kratosgado.blog.backend.repositories.jpa")
@EnableMongoRepositories(basePackages = "com.kratosgado.blog.backend.repositories.mongo")
@EnableAsync
public class BlogBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(BlogBackendApplication.class, args);
  }
}
