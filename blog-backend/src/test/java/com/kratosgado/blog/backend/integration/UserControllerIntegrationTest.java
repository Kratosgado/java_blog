package com.kratosgado.blog.backend.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.request.ChangePasswordRequest;
import com.kratosgado.blog.dtos.request.UpdateUserAvatarRequest;
import com.kratosgado.blog.dtos.request.UpdateUserProfileRequest;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for User Controller endpoints. Tests user profile management, avatar updates,
 * password changes, and role management.
 *
 * <p>Coverage:
 *
 * <ul>
 *   <li>Update user profile (username, bio, website, location)
 *   <li>Update user avatar
 *   <li>Change password with validation
 *   <li>Update user role (Admin only)
 *   <li>Authorization checks (users can only modify their own data)
 *   <li>Input validation
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Controller Integration Tests")
public class UserControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private static final String USERS_BASE_URL = "/v1/users";

  // Test user constants - IDs will be set after saving
  private Long TEST_USER_ID;
  private static final String TEST_USER_EMAIL = "testuser@example.com";
  private Long TEST_OTHER_USER_ID;
  private static final String TEST_OTHER_USER_EMAIL = "otheruser@example.com";
  private Long TEST_ADMIN_ID;
  private static final String TEST_ADMIN_EMAIL = "admin@example.com";

  private User testUser;
  private User otherUser;
  private User adminUser;

  @BeforeEach
  @Override
  void baseSetUp() {
    // Clean up database
    userRepository.deleteAll();

    // Create test user (READER role)
    testUser = new User();
    testUser.setEmail(TEST_USER_EMAIL);
    testUser.setUsername("testuser");
    testUser.setPassword(passwordEncoder.encode("password123"));
    testUser.setRole(UserRole.READER);
    testUser = userRepository.save(testUser);
    TEST_USER_ID = testUser.getId();

    // Create other user (READER role)
    otherUser = new User();
    otherUser.setEmail(TEST_OTHER_USER_EMAIL);
    otherUser.setUsername("otheruser");
    otherUser.setPassword(passwordEncoder.encode("password123"));
    otherUser.setRole(UserRole.READER);
    otherUser = userRepository.save(otherUser);
    TEST_OTHER_USER_ID = otherUser.getId();

    // Create admin user (ADMIN role)
    adminUser = new User();
    adminUser.setEmail(TEST_ADMIN_EMAIL);
    adminUser.setUsername("adminuser");
    adminUser.setPassword(passwordEncoder.encode("password123"));
    adminUser.setRole(UserRole.ADMIN);
    adminUser = userRepository.save(adminUser);
    TEST_ADMIN_ID = adminUser.getId();
  }

  @Nested
  @DisplayName("Update Profile Tests")
  class UpdateProfileTests {

    @Test
    @DisplayName("Should update own profile successfully")
    void updateProfile_OwnProfile_ShouldReturn200() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      UpdateUserProfileRequest request =
          new UpdateUserProfileRequest(
              "newusername",
              "Updated bio about myself",
              "https://newwebsite.com",
              "New York, USA");

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/profile")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.username").value("newusername"))
          .andExpect(jsonPath("$.data.bio").value("Updated bio about myself"))
          .andExpect(jsonPath("$.data.website").value("https://newwebsite.com"))
          .andExpect(jsonPath("$.data.location").value("New York, USA"));
    }

    @Test
    @DisplayName("Should update profile with partial data")
    void updateProfile_PartialData_ShouldReturn200() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      UpdateUserProfileRequest request = new UpdateUserProfileRequest("newusername", null, null, null);

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/profile")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.username").value("newusername"));
    }

    @Test
    @DisplayName("Should fail to update profile without authentication")
    void updateProfile_WithoutAuth_ShouldReturn401() throws Exception {
      UpdateUserProfileRequest request =
          new UpdateUserProfileRequest("newusername", "bio", "website", "location");

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/profile")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "a", ""})
    @DisplayName("Should fail with invalid username length")
    void updateProfile_InvalidUsernameLength_ShouldReturn400(String username) throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      UpdateUserProfileRequest request = new UpdateUserProfileRequest(username, null, null, null);

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/profile")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail when bio exceeds max length")
    void updateProfile_BioTooLong_ShouldReturn400() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      String longBio = "a".repeat(501); // Max is 500
      UpdateUserProfileRequest request = new UpdateUserProfileRequest(null, longBio, null, null);

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/profile")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({
      "validuser,This is my bio,https://mysite.com,San Francisco",
      "another_user,Short bio,https://example.org,London UK",
      "user123,null,null,Tokyo Japan"
    })
    @DisplayName("Should update profile with various valid inputs")
    void updateProfile_VariousValidInputs_ShouldReturn200(
        String username, String bio, String website, String location) throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      String bioValue = "null".equals(bio) ? null : bio;
      String websiteValue = "null".equals(website) ? null : website;

      UpdateUserProfileRequest request =
          new UpdateUserProfileRequest(username, bioValue, websiteValue, location);

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/profile")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.username").value(username));
    }
  }

  @Nested
  @DisplayName("Update Avatar Tests")
  class UpdateAvatarTests {

    @Test
    @DisplayName("Should update own avatar successfully")
    void updateAvatar_OwnAvatar_ShouldReturn200() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      UpdateUserAvatarRequest request =
          new UpdateUserAvatarRequest(TEST_USER_ID, "https://newavatar.com/image.jpg");

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/avatar")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.avatarUrl").value("https://newavatar.com/image.jpg"));
    }

    @Test
    @DisplayName("Should fail to update other user's avatar")
    void updateAvatar_OtherUsersAvatar_ShouldReturn403() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      UpdateUserAvatarRequest request =
          new UpdateUserAvatarRequest(TEST_OTHER_USER_ID, "https://newavatar.com/image.jpg");

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_OTHER_USER_ID + "/avatar")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to update avatar without authentication")
    void updateAvatar_WithoutAuth_ShouldReturn401() throws Exception {
      UpdateUserAvatarRequest request =
          new UpdateUserAvatarRequest(TEST_USER_ID, "https://newavatar.com/image.jpg");

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/avatar")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should fail with blank avatar URL")
    void updateAvatar_BlankUrl_ShouldReturn400(String avatarUrl) throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      String requestJson =
          String.format(
              "{\"userId\": %d, \"avatarUrl\": \"%s\"}", TEST_USER_ID, avatarUrl);

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/avatar")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestJson))
          .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "https://example.com/avatar1.jpg",
          "https://cdn.example.com/users/avatar.png",
          "https://storage.googleapis.com/bucket/image.gif"
        })
    @DisplayName("Should update avatar with various valid URLs")
    void updateAvatar_VariousValidUrls_ShouldReturn200(String avatarUrl) throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      UpdateUserAvatarRequest request = new UpdateUserAvatarRequest(TEST_USER_ID, avatarUrl);

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/avatar")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.avatarUrl").value(avatarUrl));
    }
  }

  @Nested
  @DisplayName("Change Password Tests")
  class ChangePasswordTests {

    @Test
    @DisplayName("Should successfully change own password")
    void changePassword_OwnPassword_ShouldReturn200() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      ChangePasswordRequest request =
          new ChangePasswordRequest(
              TEST_USER_ID, "password123", "NewSecureP@ssw0rd456", "NewSecureP@ssw0rd456");

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/password")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should fail to change other user's password")
    void changePassword_OtherUsersPassword_ShouldReturn403() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      ChangePasswordRequest request =
          new ChangePasswordRequest(
              TEST_OTHER_USER_ID,
              "OldP@ssw0rd123",
              "NewSecureP@ssw0rd456",
              "NewSecureP@ssw0rd456");

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_OTHER_USER_ID + "/password")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to change password without authentication")
    void changePassword_WithoutAuth_ShouldReturn401() throws Exception {
      ChangePasswordRequest request =
          new ChangePasswordRequest(
              TEST_USER_ID, "OldP@ssw0rd123", "NewSecureP@ssw0rd456", "NewSecureP@ssw0rd456");

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/password")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail when passwords don't match")
    void changePassword_PasswordMismatch_ShouldReturn400() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      ChangePasswordRequest request =
          new ChangePasswordRequest(
              TEST_USER_ID, "OldP@ssw0rd123", "NewSecureP@ssw0rd456", "DifferentP@ssw0rd789");

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/password")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"weak", "12345678", "password", "abcdefgh"})
    @DisplayName("Should fail with weak passwords")
    void changePassword_WeakPassword_ShouldReturn400(String newPassword) throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      ChangePasswordRequest request =
          new ChangePasswordRequest(TEST_USER_ID, "OldP@ssw0rd123", newPassword, newPassword);

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/password")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail with incorrect old password")
    void changePassword_IncorrectOldPassword_ShouldReturn400() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      ChangePasswordRequest request =
          new ChangePasswordRequest(
              TEST_USER_ID, "WrongOldP@ssw0rd", "NewSecureP@ssw0rd456", "NewSecureP@ssw0rd456");

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/password")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Update Role Tests")
  class UpdateRoleTests {

    @ParameterizedTest
    @EnumSource(UserRole.class)
    @DisplayName("Should allow admin to update user role to any value")
    void updateUserRole_AsAdmin_ShouldReturn200(UserRole role) throws Exception {
      String token = generateToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL, UserRole.ADMIN);

      mockMvc
          .perform(
              post(USERS_BASE_URL + "/" + TEST_USER_ID + "/role")
                  .header("Authorization", token)
                  .param("role", role.name()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.role").value(role.name()));
    }

    @Test
    @DisplayName("Should fail when non-admin tries to update role")
    void updateUserRole_AsNonAdmin_ShouldReturn403() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);

      mockMvc
          .perform(
              post(USERS_BASE_URL + "/" + TEST_OTHER_USER_ID + "/role")
                  .header("Authorization", token)
                  .param("role", UserRole.AUTHOR.name()))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to update role without authentication")
    void updateUserRole_WithoutAuth_ShouldReturn401() throws Exception {
      mockMvc
          .perform(
              post(USERS_BASE_URL + "/" + TEST_USER_ID + "/role")
                  .param("role", UserRole.AUTHOR.name()))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail with invalid role value")
    void updateUserRole_InvalidRole_ShouldReturn400() throws Exception {
      String token = generateToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL, UserRole.ADMIN);

      mockMvc
          .perform(
              post(USERS_BASE_URL + "/" + TEST_USER_ID + "/role")
                  .header("Authorization", token)
                  .param("role", "INVALID_ROLE"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail when role parameter is missing")
    void updateUserRole_MissingRole_ShouldReturn400() throws Exception {
      String token = generateToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL, UserRole.ADMIN);

      mockMvc
          .perform(
              post(USERS_BASE_URL + "/" + TEST_USER_ID + "/role")
                  .header("Authorization", token))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail when updating role for non-existent user")
    void updateUserRole_NonExistentUser_ShouldReturn404() throws Exception {
      String token = generateToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL, UserRole.ADMIN);
      Long nonExistentUserId = 99999L;

      mockMvc
          .perform(
              post(USERS_BASE_URL + "/" + nonExistentUserId + "/role")
                  .header("Authorization", token)
                  .param("role", UserRole.AUTHOR.name()))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Authorization Edge Cases")
  class AuthorizationEdgeCaseTests {

    @Test
    @DisplayName("Admin should not be able to update other user's profile directly")
    void admin_CannotUpdateOtherUsersProfile() throws Exception {
      String token = generateToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL, UserRole.ADMIN);
      UpdateUserProfileRequest request =
          new UpdateUserProfileRequest("hackedusername", null, null, null);

      // Admin trying to update TEST_USER_ID profile but authenticated as ADMIN_ID
      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/profile")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User can only update their own avatar")
    void user_CanOnlyUpdateOwnAvatar() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      UpdateUserAvatarRequest request =
          new UpdateUserAvatarRequest(TEST_USER_ID, "https://avatar.com/img.jpg");

      // User trying to update other user's avatar
      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_OTHER_USER_ID + "/avatar")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User can only change their own password")
    void user_CanOnlyChangeOwnPassword() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      ChangePasswordRequest request =
          new ChangePasswordRequest(
              TEST_USER_ID, "OldP@ssw0rd123", "NewSecureP@ssw0rd456", "NewSecureP@ssw0rd456");

      // User trying to change other user's password
      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_OTHER_USER_ID + "/password")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Input Validation Tests")
  class InputValidationTests {

    @Test
    @DisplayName("Should reject username with over 50 characters")
    void updateProfile_UsernameTooLong_ShouldReturn400() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      String longUsername = "a".repeat(51); // Max is 50
      UpdateUserProfileRequest request = new UpdateUserProfileRequest(longUsername, null, null, null);

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/profile")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject website URL over 200 characters")
    void updateProfile_WebsiteTooLong_ShouldReturn400() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      String longWebsite = "https://example.com/" + "a".repeat(201); // Max is 200
      UpdateUserProfileRequest request =
          new UpdateUserProfileRequest(null, null, longWebsite, null);

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/profile")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject location over 100 characters")
    void updateProfile_LocationTooLong_ShouldReturn400() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      String longLocation = "a".repeat(101); // Max is 100
      UpdateUserProfileRequest request =
          new UpdateUserProfileRequest(null, null, null, longLocation);

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/profile")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({
      "abc,Valid username with exactly 3 chars",
      "a_very_long_username_that_is_exactly_fifty_char,Valid username with exactly 50 chars",
      "user_with_special_123,Valid username with underscores and numbers"
    })
    @DisplayName("Should accept usernames at boundary lengths")
    void updateProfile_BoundaryUsernames_ShouldReturn200(String username, String description)
        throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      UpdateUserProfileRequest request = new UpdateUserProfileRequest(username, null, null, null);

      mockMvc
          .perform(
              put(USERS_BASE_URL + "/" + TEST_USER_ID + "/profile")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.username").value(username));
    }
  }
}
