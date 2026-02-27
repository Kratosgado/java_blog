package com.kratosgado.blog.backend.security;

import com.kratosgado.blog.backend.exceptions.UnauthorizedException;
import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.dtos.response.AuthResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtUtil {

  private final SecretKey signingKey;
  private final long expiration;

  public JwtUtil(
      @Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") Long expiration) {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
      throw new SecurityException("JWT secret must be at least 32 bytes");
    }
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    this.expiration = expiration;
  }

  public String extractSub(String token) {
    return extractAllClaims(token).getSubject();
  }

  public Date extractExpiration(String token) {
    return extractAllClaims(token).getExpiration();
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private String createToken(Map<String, Object> claims, String subject) {
    Instant now = Instant.now();
    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(expiration)))
        .signWith(signingKey)
        .compact();
  }

  public JwtPayload extractPayload(String token) {

    Claims claims = extractAllClaims(token);
    if (claims.getExpiration().before(new Date())) {
      throw new UnauthorizedException("Token has expired");
    }

    return new JwtPayload(
        claims.get("email", String.class),
        Long.valueOf(claims.getSubject()),
        claims.get("role", String.class),
        claims.getExpiration());
  }

  public boolean validateToken(String token, String sub) {
    return extractSub(token).equals(sub) && !isTokenExpired(token);
  }

  public AuthResponse signToken(User user) {
    Map<String, Object> claims = Map.of("email", user.getEmail(), "role", user.getRole().name());
    String token = createToken(claims, user.getId().toString());
    return new AuthResponse(
        token, user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
  }

  public AuthResponse signToken(CustomOAuth2User user) {
    Map<String, Object> claims = Map.of("email", user.getEmail(), "role", user.getRole());
    String token = createToken(claims, user.getUserId().toString());
    return new AuthResponse(
        token, user.getUserId(), user.getUsername(), user.getEmail(), user.getRole().name());
  }

  public record JwtPayload(String email, Long userId, String role, Date expiration) {}
}
