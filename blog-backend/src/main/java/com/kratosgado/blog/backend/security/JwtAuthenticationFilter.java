package com.kratosgado.blog.backend.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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

  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String username;

    // If no Authorization header or doesn't start with "Bearer ", skip this filter
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // Extract JWT token
    jwt = authHeader.substring(7);

    try {
      // Extract username from JWT
      username = jwtUtil.extractUsername(jwt);

      // If username is not null and no authentication is set in the context
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        // Load user from database
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
          User user = userOptional.get();

          // Validate token
          if (jwtUtil.validateToken(jwt, username)) {
            // Create authentication token
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authToken);
          }
        }
      }
    } catch (Exception e) {
      // Log the exception but continue with the filter chain
      logger.error("JWT authentication failed", e);
    }

    filterChain.doFilter(request, response);
  }
}
