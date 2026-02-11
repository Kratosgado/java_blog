package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.security.CustomOAuth2User;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.models.User;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom OAuth2 User Service for handling OAuth2 authentication
 *
 * <p>Responsibilities: - Fetch user details from OAuth2 provider (Google) - Create new user in
 * database on first login - Update existing user information on subsequent logins - Auto-assign
 * READER role to new OAuth2 users - Map OAuth2 attributes to User entity
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauth2User = super.loadUser(userRequest);
    return processOAuth2User(userRequest, oauth2User);
  }

  /**
   * Process OAuth2 user: create or update user in database
   *
   * @param userRequest OAuth2 user request containing client registration details
   * @param oauth2User OAuth2 user details from provider
   * @return CustomOAuth2User with user details and authorities
   */
  private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
    log.info("Processing OAuth2 user: {}", oauth2User.getName());
    String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google"
    Map<String, Object> attributes = oauth2User.getAttributes();

    // Extract user information based on provider
    OAuth2UserInfo userInfo = extractUserInfo(registrationId, attributes);

    // Check if user exists by provider ID
    Optional<User> userOptional =
        userRepository.findByAuthProviderAndProviderId(registrationId, userInfo.providerId());

    User user;
    if (userOptional.isPresent()) {
      user = userOptional.get();
      log.info("OAuth2 user found: {} ({})", user.getEmail(), registrationId);
      // Update user information (email, name, avatar may have changed)
      user = updateExistingUser(user, userInfo);
    } else {
      // Create new user
      log.info("Creating new OAuth2 user: {} ({})", userInfo.email(), registrationId);
      user = createNewUser(registrationId, userInfo);
    }

    return new CustomOAuth2User(user, oauth2User.getAttributes(), oauth2User.getAuthorities());
  }

  /**
   * Extract user information from OAuth2 attributes based on provider
   *
   * @param registrationId OAuth2 provider ID (google, github, etc.)
   * @param attributes OAuth2 user attributes from provider
   * @return OAuth2UserInfo containing extracted user details
   */
  private OAuth2UserInfo extractUserInfo(String registrationId, Map<String, Object> attributes) {
    switch (registrationId.toLowerCase()) {
      case "google":
        return extractGoogleUserInfo(attributes);
      // Add more providers here (GitHub, Facebook, etc.)
      default:
        throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
    }
  }

  /**
   * Extract user information from Google OAuth2 attributes
   *
   * @param attributes Google OAuth2 user attributes
   * @return OAuth2UserInfo with Google user details
   */
  private OAuth2UserInfo extractGoogleUserInfo(Map<String, Object> attributes) {
    String providerId = (String) attributes.get("sub");
    String email = (String) attributes.get("email");
    String name = (String) attributes.get("name");
    String picture = (String) attributes.get("picture");
    Boolean emailVerified = (Boolean) attributes.get("email_verified");

    if (providerId == null || email == null) {
      throw new OAuth2AuthenticationException(
          "Missing required Google user attributes (sub, email)");
    }

    if (Boolean.FALSE.equals(emailVerified)) {
      throw new OAuth2AuthenticationException("Email not verified with Google");
    }

    return new OAuth2UserInfo(providerId, email, name, picture);
  }

  /**
   * Create new user from OAuth2 information
   *
   * @param provider OAuth2 provider name (google, github, etc.)
   * @param userInfo Extracted OAuth2 user information
   * @return Created and persisted User entity
   */
  private User createNewUser(String provider, OAuth2UserInfo userInfo) {
    // Generate unique username from email
    String username = generateUniqueUsername(userInfo.email());

    // Create user with READER role
    User user =
        User.builder()
            .username(username)
            .email(userInfo.email())
            .avatarUrl(userInfo.avatarUrl())
            .authProvider(provider)
            .providerId(userInfo.providerId())
            .password(null) // OAuth2 users don't have passwords
            .role(UserRole.READER) // Auto-assign READER role
            .build();

    // Save user
    user = userRepository.save(user);
    log.info("Created new OAuth2 user: {} with READER role", user.getEmail());

    return user;
  }

  /**
   * Update existing user with latest OAuth2 information
   *
   * @param existingUser Existing user from database
   * @param userInfo Latest OAuth2 user information
   * @return Updated and persisted User entity
   */
  private User updateExistingUser(User existingUser, OAuth2UserInfo userInfo) {
    boolean updated = false;

    // Update email if changed
    if (!existingUser.getEmail().equals(userInfo.email())) {
      existingUser.setEmail(userInfo.email());
      updated = true;
    }

    // Update avatar URL if changed
    if (userInfo.avatarUrl() != null && !userInfo.avatarUrl().equals(existingUser.getAvatarUrl())) {
      existingUser.setAvatarUrl(userInfo.avatarUrl());
      updated = true;
    }

    if (updated) {
      existingUser = userRepository.save(existingUser);
      log.info("Updated OAuth2 user: {}", existingUser.getEmail());
    }

    return existingUser;
  }

  /**
   * Generate unique username from email Appends number if username already exists
   *
   * @param email User email address
   * @return Unique username
   */
  private String generateUniqueUsername(String email) {
    String baseUsername = email.split("@")[0];
    String username = baseUsername;
    int counter = 1;

    while (userRepository.findByUsername(username).isPresent()) {
      username = baseUsername + counter;
      counter++;
    }

    return username;
  }

  /** Internal DTO for OAuth2 user information */
  private static record OAuth2UserInfo(
      String providerId, String email, String name, String avatarUrl) {}
}
