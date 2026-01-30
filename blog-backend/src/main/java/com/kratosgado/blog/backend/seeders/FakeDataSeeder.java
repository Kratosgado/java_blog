package com.kratosgado.blog.backend.seeders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
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

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

@Component
@Profile("dev")
@Slf4j
public class FakeDataSeeder implements CommandLineRunner {

  private final Faker faker = new Faker();
  private final Random random = new Random();
  private final UserRepository userRepository;
  private final AuthService authService;
  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;

  public FakeDataSeeder(
      UserRepository userRepository,
      AuthService authService,
      PostRepository postRepository,
      CategoryRepository categoryRepository,
      TagRepository tagRepository,
      CommentRepository commentRepository,
      ReviewRepository reviewRepository) {
    this.userRepository = userRepository;
    this.authService = authService;
    this.postRepository = postRepository;
    this.categoryRepository = categoryRepository;
    this.tagRepository = tagRepository;
    this.commentRepository = commentRepository;
    this.reviewRepository = reviewRepository;
  }

  @Override
  @Transactional
  public void run(String... args) {
    // Check if data already exists
    if (userRepository.count() > 0) {
      log.info("Data already exists, skipping seeding.");
      return;
    }

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

  private List<User> seedUsers(int count) {
    List<User> users = new ArrayList<>();

    // Real user
    User real = createRealUser();
    users.add(registerUser(real, "28935617Aa@", true));

    // Fake users
    int idx = 0;
    while (users.size() < count) {
      User u = createFakeUser(idx++);
      try {
        users.add(registerUser(u, "password123@", false));
      } catch (Exception e) {
        // Skip duplicates
      }
    }

    return users;
  }

  private User createRealUser() {
    return User.builder()
        .username("kratos")
        .email("kratos@gmail.com")
        .avatarUrl("https://avatars.githubusercontent.com/u/10137?v=4")
        .build();
  }

  private User createFakeUser(int index) {
    String firstName = faker.name().firstName();
    String lastName = faker.name().lastName();
    String username = (firstName + lastName + index).toLowerCase();

    return User.builder()
        .username(username)
        .email(faker.internet().emailAddress(username))
        .avatarUrl(faker.avatar().image())
        .build();
  }

  private List<Category> seedCategories(int count) {
    List<Category> categories = new ArrayList<>();
    Set<String> names = new HashSet<>();

    while (categories.size() < count) {
      String name = faker.commerce().department();
      if (!names.add(name))
        continue;

      Category category = Category.builder()
          .name(name)
          .slug(BlogUtils.toSlug(name))
          .description(faker.lorem().sentence(20))
          .build();
      categories.add(categoryRepository.save(category));
    }

    return categories;
  }

  private List<Tag> seedTags(int count) {
    List<Tag> tags = new ArrayList<>();
    Set<String> names = new HashSet<>();

    while (tags.size() < count) {
      String name = faker.commerce().productName().split(" ")[0];
      if (!names.add(name))
        continue;

      Tag tag = new Tag();
      tag.setName(name);
      tag.setSlug(BlogUtils.toSlug(name));
      tag.setDescription(faker.lorem().sentence(10));
      tags.add(tagRepository.save(tag));
    }

    return tags;
  }

  private List<Post> seedPosts(int count, List<User> users, List<Category> categories, List<Tag> tags) {
    List<Post> posts = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      Post post = new Post();
      String title = faker.lorem().sentence();
      post.setTitle(title);
      post.setSlug(BlogUtils.toSlug(title) + "-" + i);
      post.setContent(generateBlogContent());
      post.setExcerpt(faker.lorem().sentence(20));
      post.setStatus(faker.options().option(PostStatus.published, PostStatus.draft));
      post.setViews(faker.number().numberBetween(0, 5000));
      post.setCoverImage(faker.internet().image());

      post.setUser(getRandomElement(users));
      post.setCategory(getRandomElement(categories));

      // Attach random tags (1-3)
      int tagCount = faker.number().numberBetween(1, 4);
      List<Tag> postTags = new ArrayList<>();
      for (int j = 0; j < tagCount; j++) {
        postTags.add(getRandomElement(tags));
      }
      post.setTags(postTags);

      posts.add(postRepository.save(post));
    }

    return posts;
  }

  private String generateBlogContent() {
    return faker.lorem().paragraphs(5).stream().collect(Collectors.joining("\n\n"));
  }

  private int seedComments(int count, List<Post> posts, List<User> users) {
    for (int i = 0; i < count; i++) {
      Post post = getRandomElement(posts);
      User user = getRandomElement(users);
      Comment comment = Comment.builder()
          .postId(post.getId())
          .userId(user.getId())
          .content(faker.lorem().paragraph())
          .status(CommentStatus.approved)
          .authorName(user.getUsername())
          .authorAvatarUrl(user.getAvatarUrl())
          .build();
      comment.onCreate();
      commentRepository.save(comment);
    }
    return count;
  }

  private int seedReviews(int count, List<Post> posts, List<User> users) {
    for (int i = 0; i < count; i++) {
      Post post = getRandomElement(posts);
      User user = getRandomElement(users);
      Review review = Review.builder()
          .postId(post.getId())
          .userId(user.getId())
          .rating(faker.number().numberBetween(1, 6))
          .title(faker.lorem().sentence())
          .content(faker.lorem().paragraph())
          .authorName(user.getUsername())
          .authorAvatarUrl(user.getAvatarUrl())
          .build();
      review.onCreate();
      reviewRepository.save(review);
    }
    return count;
  }

  private <T> T getRandomElement(List<T> list) {
    return list.get(random.nextInt(list.size()));
  }

  private User registerUser(User user, String passwordPlaintext, boolean admin) {
    RegisterRequest request = new RegisterRequest(
        user.getUsername(),
        user.getEmail(),
        user.getAvatarUrl(),
        passwordPlaintext,
        passwordPlaintext);

    User saved = authService.register(request);

    if (admin) {
      saved.setRole("ADMIN");
      userRepository.save(saved);
    }
    return saved;
  }
}
