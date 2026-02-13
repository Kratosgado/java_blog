package com.kratosgado.blog.backend.security;

import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.services.TokenBlacklistService;
import com.kratosgado.blog.models.User;
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
 *   <li>Token blacklist checking (for logout/revocation support)
 *   <li>Token signature and expiry validation
 *   <li>Database-backed role loading with RBAC support
 *   <li>Comprehensive security event logging
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;
  private final TokenBlacklistService tokenBlacklistService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final String token = extractTokenFromRequest(request);
    final String username;
    try {
      // CHECK 1: Token Blacklist (O(1) HashMap lookup)
      // This check comes BEFORE extracting username to fail fast for revoked tokens
      if (tokenBlacklistService.isBlacklisted(token)) {
        log.warn(
            "Authentication attempt with blacklisted token. Token prefix: {}...",
            token.substring(0, Math.min(20, token.length())));
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
      username = jwtUtil.extractUsername(token);

      // CHECK 3: Verify username exists and no authentication already set
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        // CHECK 4: Load full User entity with roles from database
        Optional<User> userOptional = userRepository.findBy(username);

        if (userOptional.isPresent()) {
          User user = userOptional.get();

          // CHECK 5: Validate token signature and expiry
          if (jwtUtil.validateToken(token, username)) {
            // Convert user role to Spring Security authority
            var authority = new SimpleGrantedAuthority(user.getRoleString());
            var authorities = java.util.List.of(authority);

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user, null, authorities);

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug(
                "JWT authentication successful for user: {} with role: {}",
                username,
                user.getRole().name());
          } else {
            // Token validation failed (signature mismatch or expired)
            log.warn(
                "JWT validation failed for user: {}. Token may be expired or tampered. "
                    + "Token prefix: {}...",
                username,
                token.substring(0, Math.min(20, token.length())));
          }
        } else {
          // User not found in database
          log.warn(
              "User not found for JWT token: {}. Token prefix: {}...",
              username,
              token.substring(0, Math.min(20, token.length())));
        }
      }

    } catch (Exception e) {
      // Catch-all for any JWT processing errors
      log.error(
          "JWT authentication failed. Token prefix: {}..., Error: {}",
          token.substring(0, Math.min(20, token.length())),
          e.getMessage(),
          e);
    }

    filterChain.doFilter(request, response);
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
