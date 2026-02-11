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

@RestController
@RequestMapping("/reader")
@Tag(
    name = "Reader (RBAC)",
    description = "Reader endpoints requiring READER, AUTHOR, or ADMIN role")
@SecurityRequirement(name = "bearerAuth")
public class ReaderController {

  @GetMapping("/bookmarks")
  @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")
  @Operation(
      summary = "Get user bookmarks",
      description =
          "Retrieve bookmarked posts for authenticated user. Requires any authenticated role.")
  public Map<String, Object> getBookmarks(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Reader access granted");
    response.put("endpoint", "GET /reader/bookmarks");
    response.put("requiredRoles", "READER, AUTHOR, or ADMIN");
    response.put("authenticatedUser", authentication.getName());
    response.put("authorities", authentication.getAuthorities());
    response.put(
        "data",
        Map.of(
            "totalBookmarks",
            8,
            "bookmarks",
            java.util.List.of(
                Map.of("postId", 101, "title", "Introduction to Spring Security"),
                Map.of("postId", 205, "title", "RBAC Best Practices"),
                Map.of("postId", 312, "title", "OAuth2 Integration Guide"))));
    return response;
  }

  @PostMapping("/bookmarks/{postId}")
  @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")
  @Operation(
      summary = "Add bookmark",
      description = "Bookmark a post. Requires any authenticated role.")
  public Map<String, Object> addBookmark(@PathVariable Long postId, Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Bookmark added successfully");
    response.put("endpoint", "POST /reader/bookmarks/{postId}");
    response.put("requiredRoles", "READER, AUTHOR, or ADMIN");
    response.put("authenticatedUser", authentication.getName());
    response.put("data", Map.of("postId", postId, "bookmarkedAt", System.currentTimeMillis()));
    return response;
  }

  @DeleteMapping("/bookmarks/{postId}")
  @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")
  @Operation(
      summary = "Remove bookmark",
      description = "Remove a bookmarked post. Requires any authenticated role.")
  public Map<String, Object> removeBookmark(
      @PathVariable Long postId, Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Bookmark removed successfully");
    response.put("endpoint", "DELETE /reader/bookmarks/{postId}");
    response.put("requiredRoles", "READER, AUTHOR, or ADMIN");
    response.put("authenticatedUser", authentication.getName());
    response.put("data", Map.of("postId", postId, "removedAt", System.currentTimeMillis()));
    return response;
  }

  @GetMapping("/profile")
  @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")
  @Operation(
      summary = "Get user profile",
      description = "Retrieve authenticated user's profile. Requires any authenticated role.")
  public Map<String, Object> getProfile(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Profile retrieved successfully");
    response.put("endpoint", "GET /reader/profile");
    response.put("requiredRoles", "READER, AUTHOR, or ADMIN");
    response.put("authenticatedUser", authentication.getName());
    response.put(
        "data",
        Map.of(
            "username",
            authentication.getName(),
            "roles",
            authentication.getAuthorities(),
            "memberSince",
            "2024-01-15",
            "postsRead",
            156,
            "commentsPosted",
            42));
    return response;
  }
}
