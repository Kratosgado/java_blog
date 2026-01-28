package com.kratosgado.blog.backend.seeders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.kratosgado.blog.backend.repositories.jdbc.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jdbc.PostRepository;
import com.kratosgado.blog.backend.repositories.jdbc.TagRepository;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.repositories.mongo.ReviewRepository;
import com.kratosgado.blog.backend.services.AuthService;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.models.User;

import at.favre.lib.crypto.bcrypt.BCrypt;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

@Component
@Profile("dev")
@Slf4j
public class FakeDataSeeder implements CommandLineRunner {

  private final Faker faker = new Faker();
  private final Random random = new Random();
  private final Connection connection;
  private final AuthService authService;
  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;

  public FakeDataSeeder(
      Connection connection,
      AuthService authService,
      PostRepository postRepository,
      CategoryRepository categoryRepository,
      TagRepository tagRepository,
      CommentRepository commentRepository,
      ReviewRepository reviewRepository) {
    this.connection = connection;
    this.authService = authService;
    this.postRepository = postRepository;
    this.categoryRepository = categoryRepository;
    this.tagRepository = tagRepository;
    this.commentRepository = commentRepository;
    this.reviewRepository = reviewRepository;
  }

  @Override
  public void run(String... args) {
    // Check if data already exists
    if (!reviewRepository.findAll().isEmpty()) {
      return;
    }
    clearDatabase();

    // Seed in order: Users -> Categories -> Tags -> Posts -> Comments -> Reviews
    List<User> users = seedUsers(20);

    List<Category> categories = seedCategories(10);

    List<Tag> tags = seedTags(6);

    List<Post> posts = seedPosts(100, users, categories, tags);

    int commentCount = seedComments(500, posts, users);

    int reviewCount = seedReviews(200, posts, users);

    log.info("Seeded dev data: users={}, categories={}, tags={}, posts={}, comments={}, reviews={}",
        users.size(), categories.size(), tags.size(), posts.size(), commentCount, reviewCount);
  }

  private void clearDatabase() {
    // Clear Mongo first
    try {
      commentRepository.findAll().forEach(c -> commentRepository.deleteById(c.getId()));
      reviewRepository.findAll().forEach(r -> reviewRepository.deleteById(r.getId()));
    } catch (Exception e) {
      log.warn("Failed clearing Mongo collections: {}", e.getMessage());
    }

    // Clear PostgreSQL (tables used by current JDBC repositories)
    try {
      connection.setAutoCommit(false);
      try (PreparedStatement stmt = connection.prepareStatement(
          "TRUNCATE TABLE post_tags, posts, tags, categories, users RESTART IDENTITY CASCADE")) {
        stmt.executeUpdate();
      }
      connection.commit();
    } catch (SQLException e) {
      try {
        connection.rollback();
      } catch (SQLException ignored) {
      }
      throw new RuntimeException("Failed to clear PostgreSQL tables", e);
    } finally {
      try {
        connection.setAutoCommit(true);
      } catch (SQLException ignored) {
      }
    }
  }

  private List<User> seedUsers(int count) {
    List<User> users = new ArrayList<>();

    // Real user
    User real = createRealUser();
    if (real.getRole() == null || real.getRole().isBlank()) {
      real.setRole("ADMIN");
    }
    users.add(registerUser(real, real.getPassword(), true));

    // Fake users (ensure unique usernames/emails)
    Set<String> usernames = new HashSet<>();
    Set<String> emails = new HashSet<>();
    usernames.add(real.getUsername());
    emails.add(real.getEmail());

    int idx = 0;
    while (users.size() < count) {
      User u = createFakeUser(idx++);
      if (!usernames.add(u.getUsername()) || !emails.add(u.getEmail())) {
        continue;
      }
      users.add(registerUser(u, "password123@", false));
    }

    return users;
  }

  private User createRealUser() {
    return User.builder()
        .username("kratos")
        .email("kratos@gmail.com")
        .password(BCrypt.withDefaults().hashToString(12, "28935617Aa@".toCharArray()))
        .avatarUrl("https://avatars.githubusercontent.com/u/10137?v=4")
        .role("USER")
        .build();
  }

  private User createFakeUser(int index) {
    String firstName = faker.name().firstName();
    String lastName = faker.name().lastName();
    String username = (firstName + lastName + index).toLowerCase();

    return User.builder()
        .username(username)
        .email(faker.internet().emailAddress(username))
        .password(BCrypt.withDefaults().hashToString(12, "28935617Aa@".toCharArray()))
        .avatarUrl(faker.avatar().image())
        .role(index == 0 ? "ADMIN" : "USER") // First user is admin
        .build();
  }

  private List<Category> seedCategories(int count) {
    Set<String> slugs = new HashSet<>();
    List<Category> categories = new ArrayList<>();

    while (categories.size() < count) {
      String name = faker.options().option(
          faker.programmingLanguage().name(),
          faker.job().field(),
          faker.book().genre(),
          faker.company().industry(),
          faker.hacker().noun());

      Category category = createFakeCategory(name);
      if (!slugs.add(category.getSlug())) {
        continue;
      }

      categories.add(categoryRepository.save(category));
    }

    return categories;
  }

  private Category createFakeCategory(String name) {
    return Category.builder()
        .name(name)
        .slug(BlogUtils.toSlug(name))
        .description(faker.lorem().sentence(20))
        .build();
  }

  private List<Tag> seedTags(int count) {
    Set<String> slugs = new HashSet<>();
    List<Tag> tags = new ArrayList<>();

    while (tags.size() < count) {
      Tag tag = createFakeTag();
      if (!slugs.add(tag.getSlug())) {
        continue;
      }
      tags.add(tagRepository.save(tag));
    }

    return tags;
  }

  private Tag createFakeTag() {
    String name = faker.options().option(
        faker.programmingLanguage().name(),
        faker.job().field(),
        faker.app().name(),
        faker.music().genre(),
        faker.color().name());
    Tag tag = new Tag();
    tag.setName(name);
    tag.setSlug(BlogUtils.toSlug(name));
    tag.setDescription(faker.lorem().sentence(10));
    return tag;
  }

  private List<Post> seedPosts(int count, List<User> users, List<Category> categories, List<Tag> tags) {
    List<Post> posts = new ArrayList<>();
    Set<String> slugs = new HashSet<>();

    while (posts.size() < count) {
      Post post = createFakePost(users, categories);

      // Ensure unique slug
      if (!slugs.add(post.getSlug())) {
        continue;
      }

      // Attach random tags (1-3)
      int tagCount = faker.number().numberBetween(1, 4);
      Set<Long> tagIds = new HashSet<>();
      while (tagIds.size() < tagCount) {
        tagIds.add(getRandomElement(tags).getId());
      }

      Post saved = postRepository.save(post);
      tagRepository.savePostTags(saved.getId(), tagIds.toArray(Long[]::new));
      posts.add(saved);

    }

    return posts;
  }

  private Post createFakePost(List<User> users, List<Category> categories) {
    Post post = new Post();

    // Basic fields
    String title = faker.lorem().sentence();
    post.setTitle(title);
    post.setSlug(BlogUtils.toSlug(title));
    post.setContent(generateBlogContent());
    post.setExcerpt(faker.lorem().sentence(20));
    post.setStatus(faker.number().numberBetween(0, 1) == 0 ? PostStatus.draft : PostStatus.published);
    post.setViews(faker.number().numberBetween(0, 5000));
    post.setCoverImage(faker.internet().image());

    // Relationships
    post.setUserId(getRandomElement(users).getId());
    post.setCategoryId(getRandomElement(categories).getId());

    return post;
  }

  private String generateBlogContent() {
    int paragraphCount = faker.number().numberBetween(5, 15);
    return IntStream.range(0, paragraphCount)
        .mapToObj(i -> {
          // Mix paragraphs with different lengths
          if (i % 3 == 0) {
            return "## " + faker.lorem().sentence() + "\n\n" + faker.lorem().paragraph(5);
          } else {
            return faker.lorem().paragraph(faker.number().numberBetween(3, 8));
          }
        })
        .collect(Collectors.joining("\n\n"));
  }

  private int seedComments(int count, List<Post> posts, List<User> users) {
    List<Comment> comments = IntStream.range(0, count)
        .mapToObj(i -> createFakeComment(posts, users))
        .collect(Collectors.toList());

    comments.forEach(comment -> commentRepository.save(comment));
    return comments.size();
  }

  private Comment createFakeComment(List<Post> posts, List<User> users) {
    Post post = getRandomElement(posts);
    User user = getRandomElement(users);

    Comment comment = new Comment();
    comment.setPostId(post.getId());
    comment.setUserId(user.getId());
    comment.setAuthorName(user.getUsername());
    comment.setAuthorAvatarUrl(user.getAvatarUrl());
    comment.setContent(faker.lorem().paragraph(faker.number().numberBetween(2, 5)));
    comment.setStatus(faker.options().option(
        CommentStatus.approved,
        CommentStatus.approved,
        CommentStatus.approved,
        CommentStatus.pending)); // 75% approved, 25% pending
    comment.setCreatedAt(generateRandomPastDate());
    comment.setUpdatedAt(comment.getCreatedAt());

    return comment;
  }

  private int seedReviews(int count, List<Post> posts, List<User> users) {
    List<Review> reviews = IntStream.range(0, count)
        .mapToObj(i -> createFakeReview(posts, users))
        .collect(Collectors.toList());

    reviews.forEach(review -> reviewRepository.save(review));
    return reviews.size();
  }

  private Review createFakeReview(List<Post> posts, List<User> users) {
    Post post = getRandomElement(posts);
    User user = getRandomElement(users);

    Review review = new Review();
    review.setPostId(post.getId());
    review.setUserId(user.getId());
    review.setAuthorName(user.getUsername());
    review.setAuthorAvatarUrl(user.getAvatarUrl());
    review.setRating(faker.number().numberBetween(1, 6)); // 1-5 stars
    review.setTitle(faker.lorem().sentence(5));
    review.setContent(faker.lorem().paragraph(faker.number().numberBetween(3, 8)));
    review.setHelpful(faker.number().numberBetween(1, 100) <= 30); // 30% marked as helpful
    review.setCreatedAt(generateRandomPastDate());
    review.setUpdatedAt(review.getCreatedAt());

    return review;
  }

  // Utility methods

  private <T> T getRandomElement(List<T> list) {
    return list.get(random.nextInt(0, list.size()));
  }

  private LocalDateTime generateRandomPastDate() {
    // Generate date within last 180 days
    int daysAgo = faker.number().numberBetween(1, 180);
    int hoursAgo = faker.number().numberBetween(0, 24);
    int minutesAgo = faker.number().numberBetween(0, 60);
    return LocalDateTime.now()
        .minusDays(daysAgo)
        .minusHours(hoursAgo)
        .minusMinutes(minutesAgo);
  }

  private User registerUser(User user, String passwordPlaintext, boolean admin) {
    RegisterRequest request = new RegisterRequest(
        user.getUsername(),
        user.getEmail(),
        user.getAvatarUrl(),
        passwordPlaintext,
        passwordPlaintext);

    User saved = authService.register(request);

    // Promote to admin when needed (RegisterRequest doesn't carry roles)
    if (admin) {
      promoteToAdmin(saved.getId());
      saved.setRole("ADMIN");
    }
    return saved;
  }

  private void promoteToAdmin(Long userId) {
    try (PreparedStatement stmt = connection.prepareStatement("UPDATE users SET role = ? WHERE id = ?")) {
      stmt.setString(1, "ADMIN");
      stmt.setLong(2, userId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed promoting user to ADMIN: " + userId, e);
    }
  }
}
