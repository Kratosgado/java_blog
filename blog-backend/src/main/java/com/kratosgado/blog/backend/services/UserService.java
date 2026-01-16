package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.request.UpdateUserProfileRequest;
import com.kratosgado.blog.models.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
  }

  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
  }

  public User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
  }

  public Page<User> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  @Transactional
  public User updateUserProfile(Long id, UpdateUserProfileRequest request, Long currentUserId) {
    if (!id.equals(currentUserId)) {
      throw new SecurityException("User not authorized to update this profile");
    }

    User user = getUserById(id);

    if (request.username() != null && !request.username().equals(user.getUsername())) {
      if (userRepository.existsByUsername(request.username())) {
        throw new IllegalArgumentException("Username already exists");
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

    return updatedUser;
  }

  @Transactional
  public User updateUserAvatar(Long id, String avatarUrl, Long currentUserId) {

    if (!id.equals(currentUserId)) {
      throw new SecurityException("User not authorized to update this avatar");
    }

    User user = getUserById(id);
    user.setAvatarUrl(avatarUrl);

    User updatedUser = userRepository.save(user);

    return updatedUser;
  }

  @Transactional
  public void changePassword(Long id, String oldPassword, String newPassword, Long currentUserId) {

    if (!id.equals(currentUserId)) {
      throw new SecurityException("User not authorized to change this password");
    }

    User user = getUserById(id);

    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw new IllegalArgumentException("Old password is incorrect");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

  }
}
