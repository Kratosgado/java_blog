
package com.kratosgado.blog.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConfig {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
  private static Dotenv dotenv = Dotenv.load();

  private static final String DB_URL = dotenv.get("DB_URL");
  private static final String USER = dotenv.get("USER");
  private static final String PASSWORD = dotenv.get("PASS");

  public static Connection getConnection() throws SQLException {
    logger.debug("Attempting database connection to: {}", DB_URL);
    return DriverManager.getConnection(DB_URL, USER, PASSWORD);
  }
}
