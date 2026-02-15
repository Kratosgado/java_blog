package com.kratosgado.blog.backend.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kratosgado.blog.backend.security.JwtUtil;
import com.kratosgado.blog.enums.UserRole;

/**
 * Base class for integration tests with common configuration.
 * Provides MockMvc, ObjectMapper, and JWT token generation utilities.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected JwtUtil jwtUtil;

  /**
   * Generates a JWT token for a test user with READER role.
   *
   * @param userId User ID
   * @param email  User email
   * @return Bearer token string
   */
  protected String generateToken(Long userId, String email) {
    return generateToken(userId, email, UserRole.READER);
  }

  /**
   * Generates a JWT token for a test user with specified role.
   *
   * @param userId User ID
   * @param email  User email
   * @param role   User role
   * @return Bearer token string
   */
  protected String generateToken(Long userId, String email, UserRole role) {
    java.util.Map<String, Object> claims = java.util.Map.of("userId", userId, "role", role.name());
    String token = jwtUtil.generateToken(userId.toString(), claims);
    return "Bearer " + token;
  }

  /**
   * Converts an object to JSON string.
   *
   * @param obj Object to convert
   * @return JSON string
   */
  protected String toJson(Object obj) throws Exception {
    return objectMapper.writeValueAsString(obj);
  }

  /**
   * Converts JSON string to object.
   *
   * @param json  JSON string
   * @param clazz Target class
   * @return Deserialized object
   */
  protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
    return objectMapper.readValue(json, clazz);
  }

  @BeforeEach
  void baseSetUp() {
    // Override in subclasses if needed
  }
}
