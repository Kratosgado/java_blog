package com.kratosgado.blog.enums;

/**
 * User roles for Role-Based Access Control (RBAC)
 *
 * <p>Role Hierarchy: - ADMIN: Full system access (user management, system configuration) - AUTHOR:
 * Content creation and management (create/edit/delete own posts) - READER: Basic user access (view
 * content, comment, like)
 */
public enum UserRole {
  /** Administrator role with full system access */
  ADMIN("Administrator", "Full system access including user management"),

  /** Author role for content creators */
  AUTHOR("Author", "Can create, edit, and publish blog posts"),

  /** Reader role for regular users */
  READER("Reader", "Can view content, comment, and interact");

  private final String displayName;
  private final String description;

  UserRole(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Get default role for new users
   *
   * @return Default role (READER)
   */
  public static UserRole getDefault() {
    return READER;
  }
}
