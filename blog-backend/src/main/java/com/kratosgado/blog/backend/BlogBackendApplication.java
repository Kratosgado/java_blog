package com.kratosgado.blog.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = { "com.kratosgado.blog.backend", "com.kratosgado.blog" })
@EntityScan(basePackages = "com.kratosgado.blog.models")
public class BlogBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(BlogBackendApplication.class, args);
  }
}
