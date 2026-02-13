package com.kratosgado.blog.backend.security;

import com.kratosgado.blog.models.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Custom OAuth2User implementation that wraps our User entity
 *
 * <p>Provides: - User entity access for controllers - OAuth2 attributes from provider - Authorities
 * from user roles (ADMIN, AUTHOR, READER)
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

  private final User user;
  private final Map<String, Object> attributes;
  private final Collection<? extends GrantedAuthority> authorities;

  public CustomOAuth2User(
      User user,
      Map<String, Object> attributes,
      Collection<? extends GrantedAuthority> oauthAuthorities) {
    this.user = user;
    this.attributes = attributes;
    // Use user role as authority, ignoring OAuth provider authorities
    this.authorities = List.of(new SimpleGrantedAuthority(user.getAuthority()));
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getName() {
    // Use email as the principal name
    return user.getEmail();
  }

  public User getUser() {
    return user;
  }

  public Long getUserId() {
    return user.getId();
  }

  public String getRole() {
    return user.getRole().name();
  }

  public String getAuthority() {
    return user.getAuthority();
  }

  public String getUsername() {
    return user.getUsername();
  }

  public String getEmail() {
    return user.getEmail();
  }
}
