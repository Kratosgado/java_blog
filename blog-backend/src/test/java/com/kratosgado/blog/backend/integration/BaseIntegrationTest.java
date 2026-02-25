package com.kratosgado.blog.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests with common configuration. Provides MockMvc, ObjectMapper, and
 * JWT token generation utilities.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

  @Autowired protected MockMvc mockMvc;

  @Autowired protected ObjectMapper objectMapper;

  @Autowired protected JwtUtil jwtUtil;

  protected String generateToken(User user) {
    String token = jwtUtil.signToken(user).token();
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
   * @param json JSON string
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
