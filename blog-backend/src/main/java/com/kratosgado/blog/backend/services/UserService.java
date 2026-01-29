package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jdbc.UserRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.UpdateUserProfileRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.UserResponse;
import com.kratosgado.blog.models.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

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
    // TODO: Implement this method
    return null;
  }

  public User updateUserAvatar(Long id, String avatarUrl, Long currentUserId) {
    // TODO: Implement this method
    return null;
  }

  public void changePassword(Long id, String oldPassword, String newPassword, Long currentUserId) {
    // TODO: Implement this method
  }

  // public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
  // // TODO: Implement this method
  // return null;
  // }

  public PageResponse<User> getAllUsers(int page, int size) {
    var users = userRepository.findAll(size, page * size);
    long totalItems = userRepository.count();
    return DtoMapper.toPageResponse(users, page, size, (int) totalItems);
  }
}
