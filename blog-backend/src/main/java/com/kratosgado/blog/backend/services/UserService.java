package com.kratosgado.blog.backend.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jdbc.UserRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.UpdateUserProfileRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.User;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User getUserById(Long id) {
    return getUserById(id, false);
  }

  public User getUserById(Long id, boolean withPassword) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("User", "id", id));
    if (!withPassword) {
      user.setPassword(null);
    }
    return user;
  }

  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> BlogException.notFound("User", "email", email));

  }

  public User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> BlogException.notFound("User", "username", username));
  }

  public User updateUserProfile(UpdateUserProfileRequest request, Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("User", "id", id));

    if (request.username() != null && !request.username().equals(user.getUsername())) {
      if (userRepository.findByUsername(request.username()).isPresent()) {
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

    User updatedUser = userRepository.update(user);
    updatedUser.setPassword(null);
    return updatedUser;
  }

  public User updateUserAvatar(Long id, String avatarUrl, Long currentUserId) {
    if (!id.equals(currentUserId)) {
      throw BlogException.forbidden("You are not authorized to update this user's avatar");
    }

    User user = userRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("User", "id", id));

    user.setAvatarUrl(avatarUrl);
    User updatedUser = userRepository.update(user);
    updatedUser.setPassword(null);
    return updatedUser;
  }

  public void changePassword(Long id, String oldPassword, String newPassword, Long currentUserId) {
    if (!id.equals(currentUserId)) {
      throw BlogException.forbidden("You are not authorized to change this user's password");
    }

    User user = userRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("User", "id", id));

    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw BlogException.badRequest("Invalid current password");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.update(user);
  }

  public PageResponse<User> getAllUsers(PageRequest pageRequest) {
    var users = userRepository.findAll(pageRequest.getSize(), pageRequest.getOffset(), pageRequest.getSortBy(),
        pageRequest.getSortDir());
    long totalItems = userRepository.count();
    return DtoMapper.toPageResponse(users, pageRequest.getPage(), pageRequest.getSize(), (int) totalItems);
  }
}
