package com.kratosgado.blog.backend.controllers;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kratosgado.blog.backend.annotations.OpenApi.GetEnpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.UserService;
import com.kratosgado.blog.dtos.request.ChangePasswordRequest;
import com.kratosgado.blog.dtos.request.UpdateUserAvatarRequest;
import com.kratosgado.blog.dtos.request.UpdateUserProfileRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.ResponseDto;
import com.kratosgado.blog.dtos.response.UserResponse;
import com.kratosgado.blog.models.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

  private final UserService userService;

  @GetMapping("/{id}")
  @Operation(summary = "Get a user by ID", description = "Retrieves a user profile by ID. Public access.")
  @GetEnpoint
  public User getUser(
      @PathVariable @Parameter(description = "User ID") Long id) {
    return userService.getUserById(id);
  }

  @GetMapping("/email/{email}")
  @Operation(summary = "Get a user by email", description = "Retrieves a user profile by email. Public access.")
  @GetEnpoint
  public User getUserByEmail(
      @PathVariable @Parameter(description = "User email") String email) {
    return userService.getUserByEmail(email);
  }

  @GetMapping
  @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users. Public access.")
  @GetEnpoint
  public PageResponse<UserResponse> getUsers(@ParameterObject Pageable pageable) {
    return userService.getAllUsers(pageable);
  }

  @PutMapping("/{id}/profile")
  @Operation(summary = "Update user profile", description = "Updates a user's profile information. Only the user can update their own profile.", security = @SecurityRequirement(name = "bearer-jwt"))
  @SecuredUpdateEndpoint
  public User updateProfile(
      @Valid @RequestBody @Parameter(description = "Profile update request") UpdateUserProfileRequest request) {
    Long id = SecurityUtils.getCurrentUserId();
    return userService.updateUserProfile(request, id);
  }

  @PutMapping("/{id}/avatar")
  @Operation(summary = "Update user avatar", description = "Updates a user's avatar. Only the user can update their own avatar.", security = @SecurityRequirement(name = "bearer-jwt"))
  @SecuredUpdateEndpoint
  public ResponseEntity<ResponseDto<User>> updateAvatar(
      @PathVariable @Parameter(description = "User ID") Long id,
      @Valid @RequestBody @Parameter(description = "Avatar update request") UpdateUserAvatarRequest request) {
    Long currentUserId = SecurityUtils.getCurrentUserId();
    User user = userService.updateUserAvatar(id, request.avatarUrl(), currentUserId);
    return ResponseEntity.ok(ResponseDto.success("Avatar updated successfully", user));
  }

  @PutMapping("/{id}/password")
  @Operation(summary = "Change password", description = "Changes a user's password. Only the user can change their own password.", security = @SecurityRequirement(name = "bearer-jwt"))
  @SecuredUpdateEndpoint
  public ResponseEntity<ResponseDto<Void>> changePassword(
      @PathVariable @Parameter(description = "User ID") Long id,
      @Valid @RequestBody @Parameter(description = "Password change request") ChangePasswordRequest request) {
    Long currentUserId = SecurityUtils.getCurrentUserId();
    userService.changePassword(id, request.oldPassword(), request.newPassword(), currentUserId);
    return ResponseEntity.ok(ResponseDto.success("Password changed successfully", null));
  }
}
