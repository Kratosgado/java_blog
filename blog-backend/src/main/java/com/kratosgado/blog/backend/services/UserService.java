package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.dao.UserDAO;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.dtos.request.UpdateUserProfileRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.UserResponse;
import com.kratosgado.blog.models.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserDAO userDAO;
  private final BCryptPasswordEncoder passwordEncoder;

  public User getUserById(Long id) {
    return getUserById(id, false);
  }

  public User getUserById(Long id, boolean withPassword) {
    User user = userDAO.getUserById(id)
        .orElseThrow(() -> BlogException.notFound("User", "id", id));
    if (!withPassword) {
      user.setPassword(null);
    }
    return user;
  }

  public User getUserByEmail(String email) {
    return userDAO.getUserByEmail(email)
        .orElseThrow(() -> BlogException.notFound("User", "email", email));
  }

  public User getUserByUsername(String username) {
    return userDAO.getUserByUsername(username)
        .orElseThrow(() -> BlogException.notFound("User", "username", username));
  }

  public User updateUserProfile(UpdateUserProfileRequest request, Long id) {
    User user = getUserById(id, true);

    if (request.username() != null && !request.username().equals(user.getUsername())) {
      if (userDAO.getUserByUsername(request.username()).isPresent()) {
        throw BlogException.duplicateResource("User", "username", request.username());
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

    if (!userDAO.updateUserProfile(user.getId(), user.getBio(), user.getWebsite(), user.getLocation())) {
      throw BlogException.internal("Failed to update user profile");
    }
    
    user.setPassword(null);
    return user;
  }

  public User updateUserAvatar(Long id, String avatarUrl, Long currentUserId) {

    if (!id.equals(currentUserId)) {
      throw BlogException.unauthorized("You are not authorized to update this avatar");
    }

    User user = getUserById(id);
    
    if (!userDAO.updateUserAvatar(id.intValue(), avatarUrl)) {
      throw BlogException.internal("Failed to update user avatar");
    }
    
    user.setAvatarUrl(avatarUrl);
    user.setPassword(null);

    return user;
  }

  public void changePassword(Long id, String oldPassword, String newPassword, Long currentUserId) {

    if (!id.equals(currentUserId)) {
      throw BlogException.unauthorized("You are not authorized to change this password");
    }

    User user = getUserById(id, true);

    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw BlogException.unauthorized("Old password is incorrect");
    }

    if (!userDAO.setUserPassword(id, passwordEncoder.encode(newPassword))) {
      throw BlogException.internal("Failed to change password");
    }
  }

  public PageResponse<UserResponse> getAllUsers(int page, int size) {
    List<User> allUsers = userDAO.getAllUsers();
    int totalElements = allUsers.size();
    int totalPages = (int) Math.ceil((double) totalElements / size);
    
    int offset = (page - 1) * size;
    int endIndex = Math.min(offset + size, totalElements);
    
    List<UserResponse> pagedUsers = allUsers.subList(offset, endIndex).stream()
        .map(user -> new UserResponse(
            user.getId().longValue(),
            user.getUsername(),
            user.getEmail(),
            user.getAvatarUrl(),
            user.getBio(),
            user.getWebsite(),
            user.getLocation(),
            user.getRole()
        ))
        .toList();
    
    return new PageResponse<>(
        pagedUsers,
        page,
        size,
        totalElements,
        totalPages,
        page < totalPages,
        page > 1
    );
  }
}
