
package com.kratosgado.blog.models;

import lombok.Data;

@Data
public class User {

  private int id;
  private String username;
  private String password;
  private String email;

  public User(String username, String password, String email) {
    this.username = username;
    this.password = password;
    this.email = email;
  }

  public User(int id, String username, String password, String email) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
  }
}
