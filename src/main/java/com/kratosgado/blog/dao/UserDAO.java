package com.kratosgado.blog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.config.DatabaseConfig;
import com.kratosgado.blog.models.User;

public class UserDAO {
  private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

  public UserDAO() {
    initDatabase();
  }

  private void initDatabase() {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      String sql = "CREATE TABLE IF NOT EXISTS users (" +
          "id SERIAL PRIMARY KEY," +
          "username VARCHAR(50) NOT NULL," +
          "password VARCHAR NOT NULL," +
          "email VARCHAR(50) UNIQUE NOT NULL," +
          "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
      stmt.executeUpdate(sql);
      logger.debug("Users table initialized successfully");
    } catch (Exception e) {
      logger.error("Failed to initialize users table", e);
    }
  }

  public boolean createUser(User user) {
    String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, user.getUsername());
      stmt.setString(2, user.getPassword());
      stmt.setString(3, user.getEmail());
      stmt.executeUpdate();
      logger.info("User created successfully: {}", user.getEmail());
      return true;
    } catch (Exception e) {
      logger.error("Failed to create user: {}", user.getEmail(), e);
      return false;
    }
  }

  public boolean setUserPassword(int id, String password) {
    String sql = "UPDATE users SET password = ? WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, password);
      stmt.setInt(2, id);
      stmt.executeUpdate();
      logger.info("Password updated for user id: {}", id);
      return true;
    } catch (Exception e) {
      logger.error("Failed to update password for user id: {}", id, e);
      return false;
    }
  }

  public Optional<User> getUserById(int id) {
    String sql = "SELECT * FROM users WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional
            .of(new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("email")));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to fetch user by id: {}", id, e);
      return Optional.empty();
    }
  }

  public Optional<User> getUserByEmail(String email) {
    String sql = "SELECT * FROM users WHERE email = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, email);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional
            .of(new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("email")));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to fetch user by email: {}", email, e);
      return Optional.empty();
    }
  }

  public boolean userEmailExists(String email) {
    return getUserByEmail(email).isPresent();
  }

}
