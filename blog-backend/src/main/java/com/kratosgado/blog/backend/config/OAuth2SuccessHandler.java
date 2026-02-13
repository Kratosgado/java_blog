package com.kratosgado.blog.backend.config;

import com.kratosgado.blog.backend.security.CustomOAuth2User;
import com.kratosgado.blog.backend.security.JwtUtil;
import com.kratosgado.blog.dtos.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtUtil jwtUtil;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    var oauth2User = (CustomOAuth2User) authentication.getPrincipal();
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", oauth2User.getUserId());
    claims.put("role", oauth2User.getRole()); // Include role: ADMIN, AUTHOR, READER

    String jwtToken = jwtUtil.generateToken(oauth2User.getEmail(), claims);
    var resBody =
        new AuthResponse(
            jwtToken,
            oauth2User.getUserId(),
            oauth2User.getUsername(),
            oauth2User.getEmail(),
            oauth2User.getRole());

    // Return JWT in response or redirect with token
    response.setContentType("application/json");
    response.getWriter().write(resBody.toJson());
  }
}
