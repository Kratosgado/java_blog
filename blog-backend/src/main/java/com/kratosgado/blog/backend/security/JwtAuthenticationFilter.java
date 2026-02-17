package com.kratosgado.blog.backend.security;

import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.services.TokenBlacklistService;
import com.kratosgado.blog.backend.models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter
 *
 * <p>Validates JWT tokens and sets Spring Security authentication context with user roles from
 * database.
 *
 * <p><b>Security Features:</b>
 *
 * <ul>
 *   <li>Token blacklist checking (for logout support)
 *   <li>Token signature and expiry validation
 *   <li>Database-backed role loading with RBAC support
 *   <li>Comprehensive security event logging
 * </ul>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;
  private final TokenBlacklistService tokenBlacklistService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String sub;

    // If no Authorization header or doesn't start with "Bearer ", skip this filter
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // Extract JWT token
    jwt = authHeader.substring(7);

    try {
      // CHECK 1: Token Blacklist (O(1) HashMap lookup)
      // This check comes BEFORE extracting username to fail fast for revoked tokens
      if (tokenBlacklistService.isBlacklisted(jwt)) {
        log.warn(
            "Authentication attempt with blacklisted token. Token prefix: {}...",
            jwt.substring(0, Math.min(20, jwt.length())));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response
            .getWriter()
            .write(
                "{\"error\": \"Token has been revoked\", "
                    + "\"message\": \"This token is no longer valid. Please login again.\"}");
        return; // Stop filter chain - do not continue
      }

      // CHECK 2: Extract username from JWT
      sub = jwtUtil.extractSub(jwt);

      // CHECK 3: Verify username exists and no authentication already set
      if (sub != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        Optional<User> userOptional = userRepository.findById(Long.valueOf(sub));

        if (userOptional.isPresent()) {
          User user = userOptional.get();
          if (jwtUtil.validateToken(jwt, sub)) {
            // Convert user role to Spring Security authority
            var authority = new SimpleGrantedAuthority(user.getAuthority());
            var authorities = java.util.List.of(authority);

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user, null, authorities);

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug(
                "JWT authentication successful for user: {} with role: {}",
                sub,
                user.getRole().name());
          } else {
            // Token validation failed (signature mismatch or expired)
            log.warn(
                "JWT validation failed for user: {}. Token may be expired or tampered. "
                    + "Token prefix: {}...",
                sub,
                jwt.substring(0, Math.min(20, jwt.length())));
          }
        } else {
          // User not found in database
          log.warn(
              "User not found for JWT token: {}. Token prefix: {}...",
              sub,
              jwt.substring(0, Math.min(20, jwt.length())));
        }
      }

    } catch (Exception e) {
      // Catch-all for any JWT processing errors
      log.error(
          "JWT authentication failed. Token prefix: {}..., Error: {}",
          jwt.substring(0, Math.min(20, jwt.length())),
          e.getMessage(),
          e);
    }

    filterChain.doFilter(request, response);
  }
}
