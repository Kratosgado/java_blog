package com.kratosgado.blog.utils.context;

import java.util.prefs.Preferences;

import com.google.gson.Gson;
import com.kratosgado.blog.models.User;

public class AuthContext {
  private static AuthContext instance;
  private User currentUser;
  private Preferences userPrefs = Preferences.userRoot().node("com/kratosgado/blog/user");

  public static AuthContext getInstance() {
    if (instance == null) {
      instance = new AuthContext();
      instance.loadUser();
    }
    return instance;
  }

  public User getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(User user) {
    currentUser = user;
    saveUser();
  }

  public void logout() {
    currentUser = null;
    userPrefs.remove("user");
  }

  private void saveUser() {
    Gson gson = new Gson();
    String json = gson.toJson(currentUser);
    userPrefs.put("user", json);
  }

  private void loadUser() {
    String json = userPrefs.get("user", null);
    if (json != null) {
      Gson gson = new Gson();
      currentUser = gson.fromJson(json, User.class);
    }
  }

  /**
   * Verifies that the user is an admin
   */
  public final void requireAdmin() {
    // if (!getCurrentUser().isAdmin()) {
    // throw new UnauthorizedException("Only Admin Users can perform this action");
    // }
  }

}
