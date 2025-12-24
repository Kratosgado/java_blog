
package com.kratosgado.blog.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConfig {
  private static Dotenv dotenv = Dotenv.load();

  private static final String DB_URL = dotenv.get("DB_URL");
  private static final String USER = dotenv.get("USER");
  private static final String PASSWORD = dotenv.get("PASS");

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(DB_URL, USER, PASSWORD);
  }
}
