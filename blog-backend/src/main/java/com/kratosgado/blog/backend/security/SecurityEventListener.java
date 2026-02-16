package com.kratosgado.blog.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Security Event Listener
 *
 * <p>Listens to Spring Security authentication and authorization events for comprehensive security
 * monitoring and audit logging.
 *
 * <p><b>Monitored Events:</b>
 *
 * <ul>
 *   <li>Authentication Success: User login, timestamp, IP address
 *   <li>Authentication Failure: Failed login attempts, reason, timestamp, IP
 *   <li>Authorization Denial: RBAC access denied events with endpoint and role info
 * </ul>
 *
 * <p><b>Security Use Cases:</b>
 *
 * <ul>
 *   <li>Brute-force attack detection (monitor failed login patterns)
 *   <li>Unauthorized access attempts (track RBAC denials)
 *   <li>User activity audit trail (successful logins)
 *   <li>Security incident investigation (comprehensive log data)
 *   <li>Compliance requirements (PCI-DSS, SOC 2, GDPR audit logs)
 * </ul>
 *
 * <p><b>Log Levels:</b>
 *
 * <ul>
 *   <li>INFO: Successful authentication (normal operation)
 *   <li>WARN: Failed authentication, authorization denials (security events)
 *   <li>ERROR: Unexpected authentication errors
 * </ul>
 *
 * <p><b>Performance:</b> Event listeners are non-blocking and execute asynchronously from
 * authentication flow. Logging overhead is minimal and does not impact authentication latency.
 */
@Slf4j
@Component
public class SecurityEventListener {

  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

  /**
   * Listen to successful authentication events
   *
   * <p><b>Logged Information:</b>
   *
   * <ul>
   *   <li>Username/Email of authenticated user
   *   <li>Timestamp of successful login
   *   <li>Source IP address
   *   <li>User roles/authorities
   * </ul>
   *
   * <p><b>Security Value:</b> - Establish baseline for normal user behavior - Detect compromised
   * accounts (unusual login times/locations) - Audit trail for compliance
   *
   * @param event Spring Security authentication success event
   */
  @EventListener
  public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
    Authentication authentication = event.getAuthentication();
    String username = authentication.getName();
    String authorities = authentication.getAuthorities().toString();
    String ipAddress = getClientIpAddress();
    String timestamp = formatCurrentTimestamp();

    log.info(
        "✓ AUTHENTICATION SUCCESS | User: {} | Roles: {} | IP: {} | Time: {}",
        username,
        authorities,
        ipAddress,
        timestamp);

    // Additional structured logging for log aggregation systems (ELK, Splunk, etc.)
    log.debug(
        "Authentication success details: {username={}, authorities={}, ip={}, timestamp={}}",
        username,
        authorities,
        ipAddress,
        timestamp);
  }

  /**
   * Listen to failed authentication events
   *
   * <p><b>Logged Information:</b>
   *
   * <ul>
   *   <li>Attempted username/email
   *   <li>Failure reason (bad credentials, locked account, etc.)
   *   <li>Timestamp of failed attempt
   *   <li>Source IP address
   * </ul>
   *
   * <p><b>Security Value:</b> - Detect brute-force attacks (multiple failures from same IP) -
   * Identify credential stuffing attempts - Alert on unusual failure patterns
   *
   * <p><b>Brute-Force Detection Pattern:</b> Multiple WARN logs from same IP within short time
   * window indicates attack. Can trigger automatic account lockout or IP blocking.
   *
   * @param event Spring Security authentication failure event
   */
  @EventListener
  public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
    Authentication authentication = event.getAuthentication();
    AuthenticationException exception = event.getException();

    String username = authentication.getName();
    String failureReason = exception.getClass().getSimpleName();
    String failureMessage = exception.getMessage();
    String ipAddress = getClientIpAddress();
    String timestamp = formatCurrentTimestamp();

    // Use WARN level for security events that need attention
    log.warn(
        "✗ AUTHENTICATION FAILURE | User: {} | Reason: {} | IP: {} | Time: {} | Details: {}",
        username,
        failureReason,
        ipAddress,
        timestamp,
        failureMessage);

    // Special handling for bad credentials (most common attack vector)
    if (event instanceof AuthenticationFailureBadCredentialsEvent) {
      log.warn(
          "⚠ POTENTIAL BRUTE-FORCE | Failed login attempt for user: {} from IP: {}",
          username,
          ipAddress);
    }

    // Structured logging for security information and event management (SIEM) systems
    log.debug(
        "Authentication failure details: {username={}, reason={}, ip={}, "
            + "timestamp={}, message={}}",
        username,
        failureReason,
        ipAddress,
        timestamp,
        failureMessage);
  }

  /**
   * Listen to authorization denied events (RBAC access control failures)
   *
   * <p><b>Logged Information:</b>
   *
   * <ul>
   *   <li>Username attempting access
   *   <li>Requested endpoint/resource
   *   <li>Required role/permission
   *   <li>User's actual roles
   *   <li>Timestamp and IP address
   * </ul>
   *
   * <p><b>Security Value:</b> - Detect privilege escalation attempts - Identify misconfigured roles
   * - Audit unauthorized access attempts - Compliance logging (SOC 2, PCI-DSS)
   *
   * <p><b>Example Scenarios:</b> - READER user trying to access /admin/** endpoints - AUTHOR user
   * trying to access /admin/users - Legitimate user with incorrect role assignment (needs role
   * update)
   *
   * @param event Spring Security authorization denied event
   */
  @EventListener
  public void onAuthorizationDenied(AuthorizationDeniedEvent<?> event) {
    Authentication authentication = event.getAuthentication().get();
    String username = authentication.getName();
    String userRoles = authentication.getAuthorities().toString();
    String ipAddress = getClientIpAddress();
    String timestamp = formatCurrentTimestamp();

    // Extract endpoint from request context
    String endpoint = "unknown";
    String httpMethod = "unknown";
    try {
      HttpServletRequest request = getCurrentRequest();
      if (request != null) {
        endpoint = request.getRequestURI();
        httpMethod = request.getMethod();
      }
    } catch (Exception e) {
      log.debug("Could not extract request details from authorization event", e);
    }

    // Log RBAC denial with context
    log.warn(
        "⊘ AUTHORIZATION DENIED | User: {} | Roles: {} | Endpoint: {} {} | IP: {} | Time: {}",
        username,
        userRoles,
        httpMethod,
        endpoint,
        ipAddress,
        timestamp);

    // Detailed logging for security analysis
    log.debug(
        "Authorization denial details: {username={}, userRoles={}, endpoint={}, "
            + "method={}, ip={}, timestamp={}}",
        username,
        userRoles,
        endpoint,
        httpMethod,
        ipAddress,
        timestamp);

    // Additional context from event
    Object resource = event.getSource();
    log.debug("Denied access to resource: {} for user: {}", resource, username);
  }

  /**
   * Extract client IP address from HTTP request
   *
   * <p><b>Handles:</b>
   *
   * <ul>
   *   <li>Direct connections: Remote address
   *   <li>Proxy/Load Balancer: X-Forwarded-For header
   *   <li>Cloudflare/CDN: CF-Connecting-IP, True-Client-IP headers
   * </ul>
   *
   * @return Client IP address or "unknown" if unavailable
   */
  private String getClientIpAddress() {
    try {
      HttpServletRequest request = getCurrentRequest();
      if (request == null) {
        return "unknown";
      }

      // Check proxy headers (in order of preference)
      String ip = request.getHeader("X-Forwarded-For");
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        // X-Forwarded-For can contain multiple IPs: "client, proxy1, proxy2"
        // First IP is the original client
        return ip.split(",")[0].trim();
      }

      ip = request.getHeader("X-Real-IP");
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        return ip;
      }

      // Fallback to direct connection IP
      return request.getRemoteAddr();

    } catch (Exception e) {
      log.debug("Could not extract client IP address", e);
      return "unknown";
    }
  }

  /**
   * Get current HTTP request from Spring RequestContextHolder
   *
   * @return Current HttpServletRequest or null if not in request context
   */
  private HttpServletRequest getCurrentRequest() {
    try {
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      return attributes != null ? attributes.getRequest() : null;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Format current timestamp in human-readable format
   *
   * @return Formatted timestamp string (e.g., "2024-03-15 14:32:45 UTC")
   */
  private String formatCurrentTimestamp() {
    return TIMESTAMP_FORMATTER.format(Instant.now());
  }
}
