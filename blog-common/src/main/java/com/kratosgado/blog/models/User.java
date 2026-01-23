package com.kratosgado.blog.models;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(nullable = false)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password;

  @Column(unique = true, nullable = false)
  private String email;

  private String avatarUrl;
  private String bio;
  private String website;
  private String location;

  @Column(nullable = false)
  @Builder.Default
  private String role = "USER";

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  @Builder.Default
  private List<Post> posts = new ArrayList<>();

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
