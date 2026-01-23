package com.kratosgado.blog.backend.graphql;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.kratosgado.blog.backend.services.UserService;
import com.kratosgado.blog.dtos.request.UpdateUserProfileRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.UserResponse;
import com.kratosgado.blog.models.User;

@Controller
public class UserGraphQLController {

  private final UserService userService;

  public UserGraphQLController(UserService userService) {
    this.userService = userService;
  }

  @QueryMapping
  public User user(@Argument Long id) {
    return userService.getUserById(id);
  }

  @QueryMapping
  public PageResponse<UserResponse> users(
      @Argument(name = "page") int page,
      @Argument(name = "size") int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    return userService.getAllUsers(pageable);
  }

  @MutationMapping
  public User updateUserProfile(@Argument Long id, @Argument UpdateUserProfileRequest input) {
    return userService.updateUserProfile(input, id);
  }
}
