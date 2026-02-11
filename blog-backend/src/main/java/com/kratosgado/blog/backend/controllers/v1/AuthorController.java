package com.kratosgado.blog.backend.controllers.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author Controller - RBAC Demo
 *
 * <p>Requires AUTHOR or ADMIN role for all endpoints
 *
 * <p>Test with:
 * - ADMIN user: Access granted (200 OK)
 * - AUTHOR user: Access granted (200 OK)
 * - READER user: Access denied (403 Forbidden)
 * - No token: Unauthorized (401)
 */
@RestController
@RequestMapping("/author")
@Tag(name = "Author (RBAC)", description = "Author endpoints requiring AUTHOR or ADMIN role")
@SecurityRequirement(name = "bearerAuth")
public class AuthorController {

  @GetMapping("/posts")
  @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
  @Operation(
      summary = "Get author's posts",
      description = "Retrieve posts created by the authenticated author. Requires AUTHOR or ADMIN role.")
  public Map<String, Object> getAuthorPosts(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Author access granted");
    response.put("endpoint", "GET /author/posts");
    response.put("requiredRoles", "AUTHOR or ADMIN");
    response.put("authenticatedUser", authentication.getName());
    response.put("authorities", authentication.getAuthorities());
    response.put("data", Map.of(
        "totalPosts", 15,
        "publishedPosts", 12,
        "draftPosts", 3,
        "totalViews", 4520
    ));
    return response;
  }

  @PostMapping("/posts/draft")
  @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
  @Operation(
      summary = "Create draft post",
      description = "Create a new draft blog post. Requires AUTHOR or ADMIN role.")
  public Map<String, Object> createDraft(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Draft created successfully");
    response.put("endpoint", "POST /author/posts/draft");
    response.put("requiredRoles", "AUTHOR or ADMIN");
    response.put("authenticatedUser", authentication.getName());
    response.put("data", Map.of(
        "draftId", 1001,
        "title", "New Draft Post",
        "status", "draft",
        "createdAt", System.currentTimeMillis()
    ));
    return response;
  }

  @PutMapping("/posts/publish")
  @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
  @Operation(
      summary = "Publish post",
      description = "Publish a draft post. Requires AUTHOR or ADMIN role.")
  public Map<String, Object> publishPost(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Post published successfully");
    response.put("endpoint", "PUT /author/posts/publish");
    response.put("requiredRoles", "AUTHOR or ADMIN");
    response.put("authenticatedUser", authentication.getName());
    response.put("data", Map.of(
        "postId", 1001,
        "title", "Published Post",
        "status", "published",
        "publishedAt", System.currentTimeMillis()
    ));
    return response;
  }

  @GetMapping("/analytics")
  @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
  @Operation(
      summary = "Get author analytics",
      description = "Retrieve analytics for author's content. Requires AUTHOR or ADMIN role.")
  public Map<String, Object> getAnalytics(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Analytics retrieved successfully");
    response.put("endpoint", "GET /author/analytics");
    response.put("requiredRoles", "AUTHOR or ADMIN");
    response.put("authenticatedUser", authentication.getName());
    response.put("data", Map.of(
        "totalViews", 4520,
        "uniqueVisitors", 2340,
        "averageReadTime", "3m 45s",
        "engagementRate", "68%"
    ));
    return response;
  }
}
