package com.kratosgado.blog.backend.config;

import com.kratosgado.blog.backend.security.JwtAccessDeniedHandler;
import com.kratosgado.blog.backend.security.JwtAuthenticationEntryPoint;
import com.kratosgado.blog.backend.security.JwtAuthenticationFilter;
import com.kratosgado.blog.backend.services.CustomOAuth2UserService;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // CSRF Protection: Disabled for stateless JWT API
        // Rationale: CSRF attacks exploit automatic cookie submission by browsers.
        // JWT tokens stored in localStorage are not automatically sent, making them
        // immune to CSRF (but vulnerable to XSS, which we mitigate via input sanitization).
        // To enable CSRF for stateful session-based auth:
        // .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public endpoints - no authentication required
                    .requestMatchers("/v*/auth/**")
                    .permitAll()
                    .requestMatchers("/docs/**")
                    .permitAll()
                    .requestMatchers("/graphiql/**", "/graphql")
                    .permitAll()
                    .requestMatchers("/metrics.html")
                    .permitAll()

                    // Public read access to content
                    .requestMatchers(
                        HttpMethod.GET,
                        "/v*/posts/**",
                        "/v*/categories/**",
                        "/v*/comments/**",
                        "/v*/tags/**",
                        "/v*/users/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/v*/users/{id}")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/data/**", "/error")
                    .permitAll()

                    // Role-based endpoint restrictions
                    .requestMatchers("/v*/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/v*/author/**")
                    .hasAnyRole("AUTHOR", "ADMIN")
                    .requestMatchers("/v*/reader/**")
                    .hasAnyRole("READER", "AUTHOR", "ADMIN")

                    // All other endpoints require authentication
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .oauth2Login(
            oauth2 ->
                oauth2
                    .loginPage("/v1/auth/google")
                    .successHandler(oAuth2SuccessHandler)
                    .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)));

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Configure allowed origins (can be overridden via CORS_ORIGINS env variable)
    String corsOrigins =
        System.getenv()
            .getOrDefault(
                "CORS_ORIGINS",
                "http://localhost:8080,http://localhost:3000,https://studio.apollographql.com");
    configuration.setAllowedOrigins(Arrays.asList(corsOrigins.split(",")));

    // Define allowed HTTP methods
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

    // Set specific allowed headers (not wildcard)
    configuration.setAllowedHeaders(
        Arrays.asList(
            "Authorization", "Content-Type", "Accept", "X-Requested-With", "Cache-Control"));

    // Enable credentials support for authenticated cross-origin requests
    configuration.setAllowCredentials(true);

    // Set max age for preflight cache (in seconds)
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
