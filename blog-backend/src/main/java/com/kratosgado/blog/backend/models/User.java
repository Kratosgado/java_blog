package com.kratosgado.blog.backend.models;

import com.kratosgado.blog.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
    name = "users",
    indexes = {
      @Index(name = "idx_users_username", columnList = "username"),
      @Index(name = "idx_users_email", columnList = "email"),
      @Index(name = "idx_users_role", columnList = "role")
    })
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(nullable = true) // Nullable for OAuth2 users
  private String password;

  @Column(unique = true, nullable = false)
  private String email;

  private String avatarUrl;

  @Column(columnDefinition = "TEXT")
  private String bio;

  private String website;
  private String location;

  // OAuth2 fields
  @Column(name = "auth_provider")
  private String authProvider; // "local", "google", "github", etc.

  @Column(name = "provider_id")
  private String providerId; // OAuth2 provider user ID

  // Role-Based Access Control (RBAC)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  @Builder.Default
  private UserRole role = UserRole.READER;

  public boolean hasRole(UserRole userRole) {
    return this.role == userRole;
  }

  /**
   * Check if user is authenticated via OAuth2
   *
   * @return true if user logged in via OAuth2 provider
   */
  public boolean isOAuth2User() {
    return authProvider != null && !authProvider.equals("local");
  }

  public String getAuthority() {
    return "ROLE_" + role.name();
  }
}
