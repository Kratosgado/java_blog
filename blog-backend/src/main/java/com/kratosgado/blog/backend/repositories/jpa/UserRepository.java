package com.kratosgado.blog.backend.repositories.jpa;

import com.kratosgado.blog.dtos.response.UserResponse;
import com.kratosgado.blog.models.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(
    path = "users",
    collectionResourceRel = "users",
    excerptProjection = UserResponse.class)
@Tag(name = "Users Data", description = "Spring Data REST endpoints for User entity management")
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<UserResponse> findByUsername(String username);

  Page<UserResponse> findAllBy(Pageable pageable);

  @RestResource(path = "by-email", rel = "by-email")
  @Operation(
      summary = "Find user by email",
      description = "Retrieves a user profile by email. Public access.")
  Optional<UserResponse> findByEmail(String email);

  @Query("SELECT u FROM User u WHERE u.email = :email")
  Optional<User> findBy(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  /**
   * Find user by OAuth2 provider and provider ID
   * @param authProvider OAuth2 provider (google, github, etc.)
   * @param providerId Provider-specific user ID
   * @return Optional containing the user if found
   */
  Optional<User> findByAuthProviderAndProviderId(String authProvider, String providerId);
}
