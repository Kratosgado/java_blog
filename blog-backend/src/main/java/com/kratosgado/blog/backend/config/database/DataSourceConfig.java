package com.kratosgado.blog.backend.config.database;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

  @Value("${spring.datasource.url}")
  private String url;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  @Value("${spring.datasource.driver-class-name}")
  private String driverClassName;

  @Bean
  public DataSource dataSource() {
    return DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .url(url)
        .username(username)
        .password(password)
        .driverClassName(driverClassName)
        .build();
  }
}
