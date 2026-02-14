package com.kratosgado.blog.backend.security;

import com.kratosgado.blog.enums.UserRole;
import java.util.Arrays;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("roleGuard")
public class RoleGuard {
  public boolean hasAnyRole(UserRole[] roles) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) return false;

    return Arrays.stream(roles)
        .anyMatch(
            role ->
                auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name())));
  }
}
