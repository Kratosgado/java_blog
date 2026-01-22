package com.kratosgado.blog.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  private Long id;
  private String username;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password;

  private String email;
  private String role;
  private String avatarUrl;
  private String bio;
  private String website;
  private String location;

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", username='" + username + '\'' +
        ", email='" + email + '\'' +
        ", avatarUrl='" + avatarUrl + '\'' +
        ", bio='" + bio + '\'' +
        ", website='" + website + '\'' +
        ", location='" + location + '\'' +
        '}';
  }
}
