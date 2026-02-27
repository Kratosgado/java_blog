package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.exceptions.*;
import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.backend.utils.PageUtil;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.UserResponse;
import com.kratosgado.blog.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final NotificationService notificationService;

  public User getUserById(Long id) {
    return getUserById(id, false);
  }

  public User getUserById(Long id, boolean withPassword) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    if (!withPassword) {
      user.setPassword(null);
    }
    return user;
  }

  public UserResponse getUserByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
  }

  public UserResponse getUserByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public User updateUserProfile(
      com.kratosgado.blog.dtos.request.UpdateUserProfileRequest request, Long id) {
    // Get the current user ID from security context
    Long currentUserId = SecurityUtils.getCurrentUserId();

    // Users can only update their own profile (even admins cannot update others'
    // profiles)
    if (!id.equals(currentUserId)) {
      throw new ForbiddenException("You are not authorized to update this user's profile");
    }

    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    if (request.username() != null && !request.username().equals(user.getUsername())) {
      if (userRepository.findByUsername(request.username()).isPresent()) {
        throw new ResourceAlreadyExistsException("User", "username", request.username());
      }
      user.setUsername(request.username());
    }

    if (request.bio() != null) {
      user.setBio(request.bio());
    }
    if (request.website() != null) {
      user.setWebsite(request.website());
    }
    if (request.location() != null) {
      user.setLocation(request.location());
    }

    User updatedUser = userRepository.save(user);
    updatedUser.setPassword(null);
    return updatedUser;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public User updateUserAvatar(Long id, String avatarUrl, Long currentUserId) {
    if (!id.equals(currentUserId)) {
      throw new ForbiddenException("You are not authorized to update this user's avatar");
    }

    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    user.setAvatarUrl(avatarUrl);
    User updatedUser = userRepository.save(user);
    updatedUser.setPassword(null);
    return updatedUser;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void changePassword(Long id, String oldPassword, String newPassword, Long currentUserId) {
    if (!id.equals(currentUserId)) {
      throw new ForbiddenException("You are not authorized to change this user's password");
    }

    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw new InvalidRequestException("Invalid current password");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  public PageResponse<UserResponse> getAllUsers(PageRequest pageRequest) {
    Pageable pageable = PageUtil.toPageable(pageRequest);
    Page<UserResponse> userPage = userRepository.findAllBy(pageable);
    return DtoMapper.toPageResponse(userPage);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public User updateUserRole(Long userId, UserRole newRole, Long adminId) {
    // Verify the current user is an admin
    User admin =
        userRepository
            .findById(adminId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

    if (admin.getRole() != UserRole.ADMIN) {
      throw new ForbiddenException("Only admins can update user roles");
    }

    // Get the target user and update their role
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    user.setRole(newRole);
    User updated = userRepository.save(user);
    updated.setPassword(null);
    return updated;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void requestPasswordReset(String email) {
    if (!userRepository.existsByEmail(email)) {
      throw new ResourceNotFoundException("User", "email", email);
    }
    // In a real implementation, we would generate a unique token, save it with expiration, etc.
    String token = java.util.UUID.randomUUID().toString();

    notificationService.sendPasswordResetNotification(email, token);
  }
}
