package com.kratosgado.blog.backend.config.database;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseConfig {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

  private static DataSource staticDataSource;

  private final DataSource dataSource;

  @Autowired
  public DatabaseConfig(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @PostConstruct
  public void init() {
    staticDataSource = this.dataSource;
    logger.info("DatabaseConfig initialized with DataSource");
  }

  public static Connection getConnection() throws SQLException {
    if (staticDataSource == null) {
      throw new IllegalStateException("DataSource not initialized yet");
    }
    logger.debug("Attempting database connection");
    return staticDataSource.getConnection();
  }
}
