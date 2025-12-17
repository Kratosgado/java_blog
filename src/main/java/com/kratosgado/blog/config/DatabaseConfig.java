
package com.kratosgado.blog.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

  private static final String DB_URL = "jdbc:postgresql://localhost:5432/blog";
  private static final String USER = "postgres";
  private static final String PASSWORD = "postgres";

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(DB_URL, USER, PASSWORD);
  }
}
