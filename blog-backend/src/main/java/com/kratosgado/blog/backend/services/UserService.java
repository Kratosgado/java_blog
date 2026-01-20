package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.request.UpdateUserProfileRequest;
import com.kratosgado.blog.dtos.response.UserResponse;
import com.kratosgado.blog.models.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

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

  public Page<UserResponse> getAllUsers(Pageable pageable) {
    return userRepository.findAllUsers(pageable);
  }

  @Transactional
  public User updateUserProfile(UpdateUserProfileRequest request, Long id) {
    User user = getUserById(id, true);

    if (request.username() != null && !request.username().equals(user.getUsername())) {
      if (userRepository.existsByUsername(request.username())) {
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

    User updatedUser = userRepository.save(user);
    updatedUser.setPassword(null);

    return updatedUser;
  }

  @Transactional
  public User updateUserAvatar(Long id, String avatarUrl, Long currentUserId) {

    if (!id.equals(currentUserId)) {
      throw BlogException.unauthorized("You are not authorized to update this avatar");
    }

    User user = getUserById(id);
    user.setAvatarUrl(avatarUrl);

    User updatedUser = userRepository.save(user);
    updatedUser.setPassword(null);

    return updatedUser;
  }

  @Transactional
  public void changePassword(Long id, String oldPassword, String newPassword, Long currentUserId) {

    if (!id.equals(currentUserId)) {
      throw BlogException.unauthorized("You are not authorized to change this password");
    }

    User user = getUserById(id, true);

    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw BlogException.unauthorized("Old password is incorrect");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

  }
}
