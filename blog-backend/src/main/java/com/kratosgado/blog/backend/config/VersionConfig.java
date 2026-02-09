package com.kratosgado.blog.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class VersionConfig implements WebMvcConfigurer {

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix("/v1",
        HandlerTypePredicate.forBasePackage("com.kratosgado.blog.backend.controllers.v1"));

    configurer.addPathPrefix("/v2",
        HandlerTypePredicate.forBasePackage("com.kratosgado.blog.backend.controllers.v2"));
  }
}
