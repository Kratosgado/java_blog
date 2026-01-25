package com.kratosgado.blog.utils.context;

import java.time.LocalDateTime;
import java.util.prefs.Preferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.utils.LocalDateTimeAdapter;

/**
 * Manages authentication context including user session and JWT token
 */
public class AuthContext {
  private static AuthContext instance;
  private User currentUser;
  private String authToken;
  private Preferences userPrefs = Preferences.userRoot().node("com/kratosgado/blog/user");

  public static AuthContext getInstance() {
    if (instance == null) {
      instance = new AuthContext();
      instance.loadSession();
    }
    return instance;
  }

  public User getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(User user) {
    currentUser = user;
    saveSession();
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String token) {
    this.authToken = token;
    saveSession();
  }

  /**
   * Set user and auth token together (called after login/register)
   */
  public void setAuthentication(User user, String token) {
    this.currentUser = user;
    this.authToken = token;
    saveSession();
  }

  public boolean isLoggedIn() {
    return currentUser != null && authToken != null;
  }

  public boolean isAdmin() {
    return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
  }

  public void logout() {
    currentUser = null;
    authToken = null;
    userPrefs.remove("user");
    userPrefs.remove("token");
  }

  private void saveSession() {
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    // Save user
    if (currentUser != null) {
      String userJson = gson.toJson(currentUser);
      userPrefs.put("user", userJson);
    } else {
      userPrefs.remove("user");
    }
    
    // Save token
    if (authToken != null) {
      userPrefs.put("token", authToken);
    } else {
      userPrefs.remove("token");
    }
  }

  private void loadSession() {
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    // Load user
    String userJson = userPrefs.get("user", null);
    if (userJson != null) {
      currentUser = gson.fromJson(userJson, User.class);
    }
    
    // Load token
    authToken = userPrefs.get("token", null);
  }

  /**
   * Verifies that the user is an admin
   */
  public final void requireAdmin() {
    if (currentUser == null) {
      throw new RuntimeException("Authentication required");
    }
    if (!isAdmin()) {
      throw new RuntimeException("Only Admin users can perform this action");
    }
  }

}
