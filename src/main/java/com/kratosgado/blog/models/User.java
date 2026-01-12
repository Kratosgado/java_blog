
package com.kratosgado.blog.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User {

  private int id;
  private String username;
  private String password;
  private String email;
  private String avatarUrl;
  private String bio;
  private String website;
  private String location;
}
