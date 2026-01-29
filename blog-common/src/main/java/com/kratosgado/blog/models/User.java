package com.kratosgado.blog.models;

import com.kratosgado.blog.interfaces.HasId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "users")
public class User implements HasId {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(unique = true, nullable = false)
  private String email;

  private String avatarUrl;

  @Column(columnDefinition = "TEXT")
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
