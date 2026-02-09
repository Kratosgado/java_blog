package com.kratosgado.blog.backend.security;

import com.kratosgado.blog.backend.exceptions.UnauthorizedException;
import com.kratosgado.blog.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

  private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

  private SecurityUtils() {
    // Utility class
  }

  public static User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    log.debug("Getting current user. Authentication: {}", authentication);
    
    if (authentication == null || !authentication.isAuthenticated()) {
      log.warn("User not authenticated. Authentication is null: {}, Is authenticated: {}", 
          authentication == null, 
          authentication != null ? authentication.isAuthenticated() : "N/A");
      throw new UnauthorizedException("User not authenticated");
    }
    
    Object principal = authentication.getPrincipal();
    log.debug("Principal type: {}, Value: {}", 
        principal != null ? principal.getClass().getName() : "null", 
        principal);
    
    if (principal instanceof User) {
      User user = (User) principal;
      log.debug("Current user retrieved: {} (ID: {})", user.getUsername(), user.getId());
      return user;
    }
    
    log.warn("Invalid authentication principal. Expected User but got: {}", 
        principal != null ? principal.getClass().getName() : "null");
    throw new UnauthorizedException("Invalid authentication principal");
  }

  public static Long getCurrentUserId() {
    return getCurrentUser().getId().longValue();
  }

  public static String getCurrentUsername() {
    return getCurrentUser().getUsername();
  }

  public static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean authenticated = authentication != null && authentication.isAuthenticated() 
        && !(authentication.getPrincipal() instanceof String);
    log.debug("Is authenticated check: {}", authenticated);
    return authenticated;
  }
}
