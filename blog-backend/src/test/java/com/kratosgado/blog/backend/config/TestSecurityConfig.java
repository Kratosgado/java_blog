package com.kratosgado.blog.backend.config;

import com.kratosgado.blog.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import static org.mockito.Mockito.mock;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Test Security Configuration without OAuth2
 *
 * <p>
 * This configuration is used for integration tests to avoid
 * OAuth2ClientRegistrationRepository
 * dependency which isn't needed for JWT authentication testing.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("test")
public class TestSecurityConfig {

  @Autowired
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  @Primary
  public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
        // CSRF disabled for stateless JWT authentication
        .csrf(AbstractHttpConfigurer::disable)

        // Session management: Stateless (JWT doesn't need sessions)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // CORS configuration
        .cors(cors -> cors.configurationSource(testCorsConfigurationSource()))

        // Authorization rules
        .authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers(
                "/v*/auth/**",
                "/v*/posts/**",
                "/v*/categories/**",
                "/v*/tags/**",
                "/v*/csrf-demo/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/graphiql",
                "/graphql")
            .permitAll()

            // Role-based access control
            .requestMatchers("/v*/admin/**").hasRole("ADMIN")
            .requestMatchers("/v*/author/**").hasAnyRole("AUTHOR", "ADMIN")
            .requestMatchers("/v*/reader/**").hasAnyRole("READER", "AUTHOR", "ADMIN")

            // All other endpoints require authentication
            .anyRequest().authenticated())

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        .build();
  }

  @Bean
  public ClientRegistrationRepository clientRegistrationRepository() {
    return mock(ClientRegistrationRepository.class);
  }

  @Bean
  @Primary
  public CorsConfigurationSource testCorsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Allow specific origins
    configuration.setAllowedOrigins(List.of(
        "http://localhost:3000",
        "http://localhost:8081",
        "http://localhost:4200"));

    // Allow specific HTTP methods
    configuration.setAllowedMethods(List.of(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

    // Allow specific headers
    configuration.setAllowedHeaders(List.of(
        "Authorization",
        "Content-Type",
        "Accept",
        "X-Requested-With",
        "Cache-Control"));

    // Allow credentials (cookies, authorization headers)
    configuration.setAllowCredentials(true);

    // Preflight cache duration (1 hour)
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  @Primary
  public BCryptPasswordEncoder testPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
