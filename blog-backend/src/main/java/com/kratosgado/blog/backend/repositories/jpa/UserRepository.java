package com.kratosgado.blog.backend.repositories.jpa;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.dtos.response.UserResponse;
import com.kratosgado.blog.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  @Query("SELECT new com.kratosgado.blog.dtos.response.UserResponse(u.id, u.username, u.email, u.avatarUrl, u.bio, u.website, u.location, u.role) FROM User u")
  Page<UserResponse> findAllUsers(Pageable pageable);
}
