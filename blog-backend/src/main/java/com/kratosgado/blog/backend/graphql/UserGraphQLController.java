package com.kratosgado.blog.backend.graphql;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<UserResponse> usersPage = userService.getAllUsers(pageRequest);

    return new PageResponse<>(
        usersPage.getContent(),
        usersPage.getNumber(),
        usersPage.getSize(),
        usersPage.getTotalElements(),
        usersPage.getTotalPages(),
        usersPage.isFirst(),
        usersPage.isLast());
  }

  @MutationMapping
  public User updateUserProfile(@Argument Long id, @Argument UpdateUserProfileRequest input) {
    return userService.updateUserProfile(input, id);
  }
}
