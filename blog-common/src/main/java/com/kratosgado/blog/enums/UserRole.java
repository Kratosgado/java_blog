package com.kratosgado.blog.enums;

/**
 * User roles for Role-Based Access Control (RBAC)
 *
 * <p>Role Hierarchy: - ADMIN: Full system access (user management, system configuration) - AUTHOR:
 * Content creation and management (create/edit/delete own posts) - READER: Basic user access (view
 * content, comment, like)
 */
public enum UserRole {
  ADMIN,
  AUTHOR,
  READER
}
