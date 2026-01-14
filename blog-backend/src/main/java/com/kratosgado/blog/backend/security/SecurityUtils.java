package com.kratosgado.blog.backend.security;

import com.kratosgado.blog.backend.exceptions.UnauthorizedException;
import com.kratosgado.blog.models.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

  private SecurityUtils() {
    // Utility class
  }

  public static User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("User not authenticated");
    }
    
    Object principal = authentication.getPrincipal();
    
    if (principal instanceof User) {
      return (User) principal;
    }
    
    throw new UnauthorizedException("Invalid authentication principal");
  }

  public static Long getCurrentUserId() {
    return getCurrentUser().getId();
  }

  public static String getCurrentUsername() {
    return getCurrentUser().getUsername();
  }

  public static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && authentication.isAuthenticated() 
        && !(authentication.getPrincipal() instanceof String);
  }
}
