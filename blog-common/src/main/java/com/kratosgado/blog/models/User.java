package com.kratosgado.blog.models;

import com.kratosgado.blog.interfaces.HasId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User implements HasId {

  private Long id;

  private String username;

  private String password;

  private String email;

  private String avatarUrl;
  private String bio;
  private String website;
  private String location;

  @Builder.Default
  private String role = "USER";

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
        ", role='" + role + '\'' +
        '}';
  }
}
