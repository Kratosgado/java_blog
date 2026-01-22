package com.kratosgado.blog.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.backend.config.database.DatabaseConfig;
import com.kratosgado.blog.models.User;

import jakarta.annotation.PostConstruct;

@Repository
@DependsOn("databaseConfig")
public class UserDAO extends BaseDAO {
  private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
  
  @Autowired
  private DatabaseConfig databaseConfig;

  @PostConstruct
  @Override
  protected void initDatabase() {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      String sql = "CREATE TABLE IF NOT EXISTS users (" +
          "id SERIAL PRIMARY KEY," +
          "username VARCHAR(50) NOT NULL UNIQUE," +
          "password VARCHAR(255) NOT NULL," +
          "email VARCHAR(100) NOT NULL UNIQUE," +
          "role VARCHAR(20) DEFAULT 'USER'," +
          "avatar_url VARCHAR(500)," +
          "bio TEXT," +
          "website VARCHAR(255)," +
          "location VARCHAR(100)," +
          "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
      stmt.executeUpdate(sql);

      createIndexes(conn);
      logger.debug("Users table initialized successfully");
    } catch (Exception e) {
      logger.error("Failed to initialize users table", e);
    }
  }

  private void createIndexes(Connection conn) {
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_users_email ON users USING HASH(LOWER(email))");
      logger.debug("Database indexes created successfully");
    } catch (Exception e) {
      logger.error("Failed to create indexes", e);
    }
  }

  public Optional<User> createUser(User user) {
    String sql = "INSERT INTO users (username, password, email, role, avatar_url) VALUES (?, ?, ?, ?, ?) RETURNING id";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, user.getUsername());
      stmt.setString(2, user.getPassword());
      stmt.setString(3, user.getEmail());
      stmt.setString(4, user.getRole() != null ? user.getRole() : "USER");
      stmt.setString(5, user.getAvatarUrl());
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        user.setId(rs.getLong("id"));
        logger.info("User created successfully: {}", user.getEmail());
        return Optional.of(user);
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to create user: {}", user.getEmail(), e);
      return Optional.empty();
    }
  }

  public boolean setUserPassword(Long id, String password) {
    String sql = "UPDATE users SET password = ? WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, password);
      stmt.setLong(2, id);
      stmt.executeUpdate();
      logger.info("Password updated for user id: {}", id);
      return true;
    } catch (Exception e) {
      logger.error("Failed to update password for user id: {}", id, e);
      return false;
    }
  }

  public Optional<User> getUserById(Long id) {
    String sql = "SELECT * FROM users WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapResultSetToUser(rs));
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
        return Optional.of(mapResultSetToUser(rs));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to fetch user by email: {}", email, e);
      return Optional.empty();
    }
  }

  public Optional<User> getUserByUsername(String username) {
    String sql = "SELECT * FROM users WHERE username = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, username);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapResultSetToUser(rs));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to fetch user by username: {}", username, e);
      return Optional.empty();
    }
  }

  public boolean userEmailExists(String email) {
    return getUserByEmail(email).isPresent();
  }

  public boolean updateUserAvatar(Integer userId, String avatarUrl) {
    String sql = "UPDATE users SET avatar_url = ? WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, avatarUrl);
      stmt.setInt(2, userId);
      stmt.executeUpdate();
      logger.info("Avatar updated for user id: {}", userId);
      return true;
    } catch (Exception e) {
      logger.error("Failed to update avatar for user id: {}", userId, e);
      return false;
    }
  }

  public boolean updateUserProfile(Long userId, String bio, String website, String location) {
    String sql = "UPDATE users SET bio = ?, website = ?, location = ? WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, bio);
      stmt.setString(2, website);
      stmt.setString(3, location);
      stmt.setLong(4, userId);
      stmt.executeUpdate();
      logger.info("Profile updated for user id: {}", userId);
      return true;
    } catch (Exception e) {
      logger.error("Failed to update profile for user id: {}", userId, e);
      return false;
    }
  }

  public List<User> getAllUsers() {
    String sql = "SELECT * FROM users ORDER BY created_at DESC";
    List<User> users = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        users.add(mapResultSetToUser(rs));
      }
      logger.debug("Fetched {} users", users.size());
    } catch (Exception e) {
      logger.error("Failed to fetch all users", e);
    }
    return users;
  }

  private User mapResultSetToUser(ResultSet rs) throws Exception {
    User user = new User();
    user.setId(rs.getLong("id"));
    user.setUsername(rs.getString("username"));
    user.setPassword(rs.getString("password"));
    user.setEmail(rs.getString("email"));
    user.setRole(rs.getString("role"));
    user.setAvatarUrl(rs.getString("avatar_url"));
    user.setBio(rs.getString("bio"));
    user.setWebsite(rs.getString("website"));
    user.setLocation(rs.getString("location"));
    return user;
  }
}
