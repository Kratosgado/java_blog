package com.kratosgado.blog.backend.aspects;

import com.kratosgado.blog.backend.annotations.OpenApi;
import com.kratosgado.blog.backend.exceptions.ForbiddenException;
import com.kratosgado.blog.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP aspect to enforce role-based access control on secured endpoints. Extracts roles
 * from @SecuredEndpoint and related annotations and validates them.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleGuardAspect {

  @Before(
      "@annotation(com.kratosgado.blog.backend.annotations.OpenApi.SecuredEndpoint) || "
          + "@annotation(com.kratosgado.blog.backend.annotations.OpenApi.SecuredGetEndpoint) || "
          + "@annotation(com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint) || "
          + "@annotation(com.kratosgado.blog.backend.annotations.OpenApi.SecuredCreateEndpoint) || "
          + "@annotation(com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint)")
  public void checkRoleAccess(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    UserRole[] roles = extractRoles(signature);

    if (!hasAnyRole(roles)) {
      throw new ForbiddenException(
          "You do not have permission to access this endpoint. Required roles: "
              + java.util.Arrays.toString(roles));
    }
  }

  public boolean hasAnyRole(UserRole[] roles) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null) return false;
    for (UserRole role : roles) {
      if (auth.getAuthorities().stream()
          .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()))) {
        return true;
      }
    }
    return false;
  }

  private UserRole[] extractRoles(MethodSignature signature) {
    var method = signature.getMethod();

    // Check for SecuredEndpoint
    var securedEndpoint = method.getAnnotation(OpenApi.SecuredEndpoint.class);
    if (securedEndpoint != null) {
      return securedEndpoint.roles();
    }

    // Check for SecuredGetEndpoint
    var securedGetEndpoint = method.getAnnotation(OpenApi.SecuredGetEndpoint.class);
    if (securedGetEndpoint != null) {
      return securedGetEndpoint.roles();
    }

    // Check for SecuredUpdateEndpoint
    var securedUpdateEndpoint = method.getAnnotation(OpenApi.SecuredUpdateEndpoint.class);
    if (securedUpdateEndpoint != null) {
      return securedUpdateEndpoint.roles();
    }

    // Check for SecuredCreateEndpoint
    var securedCreateEndpoint = method.getAnnotation(OpenApi.SecuredCreateEndpoint.class);
    if (securedCreateEndpoint != null) {
      return securedCreateEndpoint.roles();
    }

    // Check for DeleteEndpoint
    var deleteEndpoint = method.getAnnotation(OpenApi.DeleteEndpoint.class);
    if (deleteEndpoint != null) {
      return deleteEndpoint.roles();
    }

    // Default to ADMIN if no roles found
    return new UserRole[] {UserRole.ADMIN};
  }
}
