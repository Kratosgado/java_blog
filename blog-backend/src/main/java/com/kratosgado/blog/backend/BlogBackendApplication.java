package com.kratosgado.blog.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.kratosgado.blog.backend", "com.kratosgado.blog"})
public class BlogBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(BlogBackendApplication.class, args);
  }
}
