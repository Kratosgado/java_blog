package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.UserResponse;
import com.kratosgado.blog.enums.UserRole;

public interface UserService {

  User getUserById(Long id);

  User getUserById(Long id, boolean withPassword);

  UserResponse getUserByEmail(String email);

  UserResponse getUserByUsername(String username);

  User updateUserProfile(
      com.kratosgado.blog.dtos.request.UpdateUserProfileRequest request, Long id);

  User updateUserAvatar(Long id, String avatarUrl, Long currentUserId);

  void changePassword(Long id, String oldPassword, String newPassword, Long currentUserId);

  PageResponse<UserResponse> getAllUsers(PageRequest pageRequest);

  User updateUserRole(Long userId, UserRole newRole, Long adminId);
}
