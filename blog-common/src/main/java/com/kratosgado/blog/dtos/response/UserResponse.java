
package com.kratosgado.blog.dtos.response;

public record UserResponse(
    Long id,
    String username,
    String email,
    String avatarUrl,
    String bio,
    String website,
    String location,
    String role) {
}
