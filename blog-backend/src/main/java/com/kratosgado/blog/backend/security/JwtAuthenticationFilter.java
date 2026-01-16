package com.kratosgado.blog.backend.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.models.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String username;

    log.info("=== JWT Filter Invoked for: {} {} ===", request.getMethod(), request.getRequestURI());
    log.info("Authorization header: {}", authHeader != null ? "Present" : "Missing");

    // If no Authorization header or doesn't start with "Bearer ", skip this filter
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.info("No valid Authorization header, skipping JWT authentication");
      filterChain.doFilter(request, response);
      return;
    }

    // Extract JWT token
    jwt = authHeader.substring(7);
    log.info("JWT token extracted: {}...", jwt.substring(0, Math.min(20, jwt.length())));

    try {
      // Extract username from JWT
      username = jwtUtil.extractUsername(jwt);
      log.info("Username extracted from JWT: {}", username);

      // If username is not null and no authentication is set in the context
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        log.info("Attempting to authenticate user: {}", username);

        // Load user from database
        Optional<User> userOptional = userRepository.findByEmail(username);

        if (userOptional.isPresent()) {
          User user = userOptional.get();
          log.info("User found in database: {} (ID: {}, Role: {})", user.getUsername(), user.getId(), user.getRole());

          // Validate token
          if (jwtUtil.validateToken(jwt, username)) {
            log.info("JWT token validated successfully for user: {}", username);

            // Create authentication token
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.info("Authentication set in SecurityContext for user: {}", username);
          } else {
            log.warn("JWT token validation failed for user: {}", username);
          }
        } else {
          log.warn("User not found in database: {}", username);
        }
      } else {
        if (username == null) {
          log.warn("Username is null after extracting from JWT");
        } else {
          log.info("User already authenticated in SecurityContext");
        }
      }
    } catch (Exception e) {
      // Log the exception but continue with the filter chain
      log.error("JWT authentication failed with exception", e);
    }

    log.info("=== JWT Filter Complete ===");
    filterChain.doFilter(request, response);
  }
}
