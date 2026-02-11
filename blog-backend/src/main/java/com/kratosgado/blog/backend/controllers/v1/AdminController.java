package com.kratosgado.blog.backend.controllers.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin Controller - RBAC Demo
 *
 * <p>Requires ADMIN role for all endpoints
 *
 * <p>Test with:
 * - ADMIN user: Access granted (200 OK)
 * - AUTHOR/READER user: Access denied (403 Forbidden)
 * - No token: Unauthorized (401)
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "Admin (RBAC)", description = "Admin-only endpoints requiring ADMIN role")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

  @GetMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Get all users (Admin only)",
      description = "Retrieve list of all users. Requires ADMIN role.")
  public Map<String, Object> getAllUsers(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Admin access granted");
    response.put("endpoint", "GET /admin/users");
    response.put("requiredRole", "ADMIN");
    response.put("authenticatedUser", authentication.getName());
    response.put("authorities", authentication.getAuthorities());
    response.put("data", Map.of(
        "totalUsers", 42,
        "activeUsers", 35,
        "adminUsers", 3
    ));
    return response;
  }

  @PostMapping("/users/{id}/promote")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Promote user role (Admin only)",
      description = "Promote a user to AUTHOR or ADMIN. Requires ADMIN role.")
  public Map<String, Object> promoteUser(
      @PathVariable Long id,
      Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "User promoted successfully");
    response.put("endpoint", "POST /admin/users/{id}/promote");
    response.put("requiredRole", "ADMIN");
    response.put("authenticatedUser", authentication.getName());
    response.put("data", Map.of(
        "userId", id,
        "previousRole", "READER",
        "newRole", "AUTHOR"
    ));
    return response;
  }

  @DeleteMapping("/users/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Delete user (Admin only)",
      description = "Delete a user from the system. Requires ADMIN role.")
  public Map<String, Object> deleteUser(
      @PathVariable Long id,
      Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "User deleted successfully");
    response.put("endpoint", "DELETE /admin/users/{id}");
    response.put("requiredRole", "ADMIN");
    response.put("authenticatedUser", authentication.getName());
    response.put("data", Map.of(
        "userId", id,
        "deletedAt", System.currentTimeMillis()
    ));
    return response;
  }

  @GetMapping("/system/stats")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Get system statistics (Admin only)",
      description = "Retrieve system-wide statistics. Requires ADMIN role.")
  public Map<String, Object> getSystemStats(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "System statistics retrieved");
    response.put("endpoint", "GET /admin/system/stats");
    response.put("requiredRole", "ADMIN");
    response.put("authenticatedUser", authentication.getName());
    response.put("data", Map.of(
        "totalPosts", 1250,
        "totalComments", 8450,
        "serverUptime", "15d 7h 23m",
        "memoryUsage", "2.3GB / 8GB"
    ));
    return response;
  }
}
