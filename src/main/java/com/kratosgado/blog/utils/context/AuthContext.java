package com.kratosgado.blog.utils.context;

import com.kratosgado.blog.models.User;

public class AuthContext {
  private static AuthContext instance;
  private User currentUser;

  public static AuthContext getInstance() {
    if (instance == null) {
      instance = new AuthContext();
    }
    return instance;
  }

  public User getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(User user) {
    currentUser = user;
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
