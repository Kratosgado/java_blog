package com.kratosgado.blog.backend.controllers.v1;

import com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.UserService;
import com.kratosgado.blog.dtos.request.ChangePasswordRequest;
import com.kratosgado.blog.dtos.request.UpdateUserAvatarRequest;
import com.kratosgado.blog.dtos.request.UpdateUserProfileRequest;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.models.User;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

  private final UserService userService;

  @PutMapping("/{id}/profile")
  @SecuredUpdateEndpoint(
      summary = "Update user profile",
      description =
          "Updates a user's profile information. Only the user can update their own profile.")
  public User updateProfile(
      @PathVariable @Parameter(description = "User ID") Long id,
      @Valid @RequestBody @Parameter(description = "Profile update request")
          UpdateUserProfileRequest request) {
    return userService.updateUserProfile(request, id);
  }

  @PutMapping("/{id}/avatar")
  @SecuredUpdateEndpoint(
      summary = "Update user avatar",
      description = "Updates a user's avatar. Only the user can update their own avatar.")
  public User updateAvatar(
      @PathVariable @Parameter(description = "User ID") Long id,
      @Valid @RequestBody @Parameter(description = "Avatar update request")
          UpdateUserAvatarRequest request) {
    Long currentUserId = SecurityUtils.getCurrentUserId();
    return userService.updateUserAvatar(id, request.avatarUrl(), currentUserId);
  }

  @PutMapping("/{id}/password")
  @SecuredUpdateEndpoint(
      summary = "Change password",
      description = "Changes a user's password. Only the user can change their own password.")
  public void changePassword(
      @PathVariable @Parameter(description = "User ID") Long id,
      @Valid @RequestBody @Parameter(description = "Password change request")
          ChangePasswordRequest request) {
    Long currentUserId = SecurityUtils.getCurrentUserId();
    userService.changePassword(id, request.oldPassword(), request.newPassword(), currentUserId);
  }

  @PostMapping("/{id}/role")
  @SecuredUpdateEndpoint(summary = "Update user role", description = "Admin updates a user's role")
  public User updateUserRole(
      @PathVariable @Parameter(description = "User ID") Long id,
      @RequestParam(required = true) @Parameter(description = "New role for the user") UserRole role) {
    Long adminId = SecurityUtils.getCurrentUserId();
    return userService.updateUserRole(id, role, adminId);
  }
}
