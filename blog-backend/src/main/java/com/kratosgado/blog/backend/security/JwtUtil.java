package com.kratosgado.blog.backend.security;

import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.dtos.response.AuthResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private Long expiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String extractSub(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public String generateToken(String sub, Long userId) {
    Map<String, Object> claims = Map.of("userId", userId);
    return createToken(claims, sub);
  }

  public String generateToken(String sub, Map<String, Object> claims) {
    return createToken(claims, sub);
  }

  private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }

  public JwtPayload extractPayload(String token) {
    var claims =
        Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();

    String email = (String) claims.get("email");

    return new JwtPayload(
        email, Long.valueOf(claims.getSubject()), claims.get("role", String.class));
  }

  public Boolean validateToken(String token, String sub) {
    final String extractedUsername = extractSub(token);
    return (extractedUsername.equals(sub) && !isTokenExpired(token));
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

  public static record JwtPayload(String email, Long userId, String role) {}
}
