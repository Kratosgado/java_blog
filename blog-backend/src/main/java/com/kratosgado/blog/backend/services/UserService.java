package com.kratosgado.blog.backend.services;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.utils.BlogConstants;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  @Cacheable(value = BlogConstants.CacheNames.USERS, key = "#id")
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

  @Cacheable(value = BlogConstants.CacheNames.USERS, key = "#email")
  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> BlogException.notFound("User", "email", email));
  }

  @Cacheable(value = BlogConstants.CacheNames.USERS, key = "#username")
  public User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> BlogException.notFound("User", "username", username));
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @Caching(evict = {
      @CacheEvict(value = BlogConstants.CacheNames.USERS, allEntries = true),
      @CacheEvict(value = BlogConstants.CacheNames.USERLIST, allEntries = true)
  })
  public User updateUserProfile(com.kratosgado.blog.dtos.request.UpdateUserProfileRequest request, Long id) {
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

    User updatedUser = userRepository.save(user);
    updatedUser.setPassword(null);
    return updatedUser;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @Caching(evict = {
      @CacheEvict(value = BlogConstants.CacheNames.USERS, allEntries = true),
      @CacheEvict(value = BlogConstants.CacheNames.USERLIST, allEntries = true)
  })
  public User updateUserAvatar(Long id, String avatarUrl, Long currentUserId) {
    if (!id.equals(currentUserId)) {
      throw BlogException.forbidden("You are not authorized to update this user's avatar");
    }

    User user = userRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("User", "id", id));

    user.setAvatarUrl(avatarUrl);
    User updatedUser = userRepository.save(user);
    updatedUser.setPassword(null);
    return updatedUser;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @Caching(evict = {
      @CacheEvict(value = BlogConstants.CacheNames.USERS, key = "#id"),
      @CacheEvict(value = BlogConstants.CacheNames.USERLIST, allEntries = true)
  })
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
    userRepository.save(user);
  }

  @Cacheable(value = BlogConstants.CacheNames.USERLIST, key = "#pageRequest.toString()")
  public PageResponse<User> getAllUsers(PageRequest pageRequest) {
    Pageable pageable = pageRequest.toPageable();
    Page<User> userPage = userRepository.findAll(pageable);
    return DtoMapper.toPageResponse(userPage);

  }
}
