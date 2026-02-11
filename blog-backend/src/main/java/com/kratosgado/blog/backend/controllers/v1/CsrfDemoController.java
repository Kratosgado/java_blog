package com.kratosgado.blog.backend.controllers.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CSRF Demonstration Controller
 *
 * <p>This controller demonstrates how CSRF protection works in stateful, session-based
 * authentication. It's provided for educational purposes to show the difference between:
 *
 * <ul>
 *   <li>Stateless JWT API (CSRF disabled) - our main API
 *   <li>Stateful session-based auth (CSRF enabled) - this demo
 * </ul>
 *
 * <p><strong>Why CSRF is disabled for JWT APIs:</strong> CSRF attacks exploit browser-based
 * session cookies that are automatically sent with requests. JWT tokens stored in localStorage or
 * sessionStorage are not automatically sent by browsers, making them immune to CSRF attacks (but
 * vulnerable to XSS attacks instead, which is mitigated by proper input sanitization).
 *
 * <p><strong>To enable CSRF protection:</strong> In SecurityConfig, replace {@code .csrf(csrf ->
 * csrf.disable())} with proper CSRF configuration using CsrfTokenRepository.
 *
 * @see <a href="https://owasp.org/www-community/attacks/csrf">OWASP CSRF</a>
 */
@RestController
@RequestMapping("/csrf-demo")
@Tag(name = "CSRF Demo", description = "Educational endpoints demonstrating CSRF protection")
public class CsrfDemoController {

  /**
   * GET endpoint to retrieve CSRF token
   *
   * <p>In a CSRF-protected application, clients must:
   * 1. First call this endpoint to get a CSRF token
   * 2. Include the token in subsequent POST/PUT/DELETE requests
   *
   * @param request HttpServletRequest to extract CSRF token
   * @return Map containing CSRF token and usage instructions
   */
  @GetMapping("/token")
  @Operation(
      summary = "Get CSRF Token",
      description =
          "Retrieve CSRF token for form submission. "
              + "Note: CSRF is currently disabled in this API (stateless JWT). "
              + "This endpoint demonstrates what would happen if CSRF was enabled.")
  public Map<String, Object> getCsrfToken(HttpServletRequest request) {
    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

    Map<String, Object> response = new HashMap<>();

    if (csrfToken != null) {
      response.put("token", csrfToken.getToken());
      response.put("headerName", csrfToken.getHeaderName());
      response.put("parameterName", csrfToken.getParameterName());
      response.put(
          "usage",
          "Include this token in "
              + csrfToken.getHeaderName()
              + " header or "
              + csrfToken.getParameterName()
              + " parameter");
    } else {
      response.put("message", "CSRF protection is currently disabled for this stateless JWT API");
      response.put(
          "reason",
          "Stateless JWT tokens are not vulnerable to CSRF attacks "
              + "because they are not automatically sent by browsers like cookies");
    }

    response.put("csrfEnabled", csrfToken != null);

    return response;
  }

  /**
   * POST endpoint demonstrating form submission with CSRF protection
   *
   * <p>If CSRF was enabled, this endpoint would require a valid CSRF token in either:
   * - Request header (X-CSRF-TOKEN)
   * - Form parameter (_csrf)
   *
   * @param message Sample form data
   * @return Confirmation message
   */
  @PostMapping("/submit-form")
  @Operation(
      summary = "Submit Form (CSRF Demo)",
      description =
          "Demonstrates form submission that would require CSRF token if CSRF was enabled. "
              + "Currently passes through because CSRF is disabled for our stateless JWT API.")
  public Map<String, Object> submitForm(@RequestParam(defaultValue = "Test message") String message) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "Form submitted successfully");
    response.put("receivedData", message);
    response.put(
        "note",
        "This endpoint would reject requests without valid CSRF token if CSRF protection was enabled");

    return response;
  }

  /**
   * Informational endpoint explaining CSRF protection
   *
   * @return Detailed explanation of CSRF protection and why it's disabled
   */
  @GetMapping("/info")
  @Operation(
      summary = "CSRF Protection Information",
      description = "Get detailed explanation of CSRF protection and its configuration")
  public Map<String, Object> getInfo() {
    Map<String, Object> info = new HashMap<>();

    info.put("csrfProtection", "disabled");
    info.put("apiType", "Stateless JWT API");

    info.put(
        "rationale",
        Map.of(
            "csrfVulnerability",
            "CSRF attacks exploit automatic cookie submission by browsers",
            "jwtStorage",
            "JWTs are stored in localStorage/sessionStorage, not cookies",
            "browserBehavior",
            "Browsers don't automatically send Authorization headers",
            "conclusion",
            "JWT APIs are not vulnerable to traditional CSRF attacks"));

    info.put(
        "alternativeSecurity",
        Map.of(
            "xssProtection",
            "Input sanitization and output encoding (critical for JWT APIs)",
            "corsPolicy",
            "Strict CORS configuration to prevent unauthorized domains",
            "tokenExpiry",
            "Short-lived tokens with refresh token mechanism",
            "httpsOnly",
            "Always use HTTPS to prevent token interception"));

    info.put(
        "whenToEnableCsrf",
        Map.of(
            "sessionBased",
            "When using cookie-based session authentication",
            "formLogin",
            "When using Spring Security form login with sessions",
            "hybridAuth",
            "When mixing session-based and token-based authentication"));

    info.put(
        "howToEnableCsrf",
        "In SecurityConfig.java, replace .csrf(csrf -> csrf.disable()) with "
            + ".csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))");

    return info;
  }
}
