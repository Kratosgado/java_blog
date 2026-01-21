package com.kratosgado.blog.backend.seeders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.repositories.mongo.ReviewRepository;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.models.User;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class FakeDataSeeder implements CommandLineRunner {

  private final Faker faker = new Faker();
  private final Random random = new Random();
  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;

  @Transactional
  @Override
  public void run(String... args) {
    log.info("Starting data seeding...");

    // Check if data already exists
    if (userRepository.count() > 0) {
      log.info("Database already contains data. Skipping seeding.");
      return;
    }

    // Seed in order: Users -> Categories -> Tags -> Posts -> Comments -> Reviews
    List<User> users = seedUsers(20);
    log.info("Seeded {} users", users.size());

    List<Category> categories = seedCategories(10);
    log.info("Seeded {} categories", categories.size());

    List<Tag> tags = seedTags(6);
    log.info("Seeded {} tags", tags.size());

    List<Post> posts = seedPosts(100, users, categories, tags);
    log.info("Seeded {} posts", posts.size());

    int commentCount = seedComments(500, posts, users);
    log.info("Seeded {} comments", commentCount);

    int reviewCount = seedReviews(200, posts, users);
    log.info("Seeded {} reviews", reviewCount);

    log.info("Data seeding completed successfully!");
  }

  private List<User> seedUsers(int count) {
    Set<User> users = IntStream.range(0, count)
        .mapToObj(i -> createFakeUser(i))
        .collect(Collectors.toSet());
    return userRepository.saveAll(users);
  }

  private User createFakeUser(int index) {
    User user = new User();
    String firstName = faker.name().firstName();
    String lastName = faker.name().lastName();
    String username = (firstName + lastName + index).toLowerCase();

    user.setUsername(username);
    user.setEmail(faker.internet().emailAddress(username));
    user.setPassword(BCrypt.withDefaults().hashToString(12, "password123".toCharArray()));
    user.setAvatarUrl(faker.avatar().image());
    user.setBio(faker.lorem().sentence(15));
    user.setWebsite(faker.internet().url());
    user.setLocation(faker.address().city() + ", " + faker.address().country());
    user.setRole(index == 0 ? "ADMIN" : "USER"); // First user is admin

    return user;
  }

  private List<Category> seedCategories(int count) {
    List<String> categoryNames = List.of(
        "Technology", "Travel", "Food & Cooking", "Health & Fitness",
        "Business", "Lifestyle", "Entertainment", "Sports",
        "Science", "Education", "Art & Design", "Photography",
        "Music", "Fashion", "Finance", "Gaming");

    List<Category> categories = categoryNames.stream()
        .limit(count)
        .map(this::createFakeCategory)
        .collect(Collectors.toList());

    return categoryRepository.saveAll(categories);
  }

  private Category createFakeCategory(String name) {
    return Category.builder()
        .name(name)
        .slug(slugify(name))
        .description(faker.lorem().sentence(20))
        .build();
  }

  private List<Tag> seedTags(int count) {
    Set<Tag> tags = IntStream.range(0, count)
        .mapToObj(i -> createFakeTag())
        .collect(Collectors.toSet());
    return tagRepository.saveAll(tags);
  }

  private Tag createFakeTag() {
    String name = faker.options().option(
        faker.programmingLanguage().name(),
        faker.job().field(),
        faker.app().name(),
        faker.music().genre(),
        faker.color().name());
    return new Tag(name, slugify(name), faker.lorem().sentence(10));
  }

  private List<Post> seedPosts(int count, List<User> users, List<Category> categories, List<Tag> tags) {
    Set<Post> posts = IntStream.range(0, count)
        .mapToObj(i -> createFakePost(users, categories, tags))
        .collect(Collectors.toSet());
    return postRepository.saveAll(posts);
  }

  private Post createFakePost(List<User> users, List<Category> categories, List<Tag> tags) {
    Post post = new Post();

    // Basic fields
    String title = faker.book().title();
    post.setTitle(title);
    post.setSlug(slugify(title) + "-" + faker.number().numberBetween(1000, 9999));
    post.setContent(generateBlogContent());
    post.setExcerpt(faker.lorem().sentence(20));
    post.setStatus(faker.options().option(PostStatus.published, PostStatus.draft));
    post.setViews(faker.number().numberBetween(0, 5000));
    post.setLikesCount(faker.number().numberBetween(0, 500));
    post.setCoverImage(faker.internet().image());

    // Relationships
    post.setUser(getRandomElement(users));
    post.setCategory(getRandomElement(categories));

    // Add 2-5 random tags
    int tagCount = faker.number().numberBetween(2, 6);
    List<Tag> postTags = new ArrayList<>();
    // for (int i = 0; i < tagCount; i++) {
    // Tag tag = getRandomElement(tags);
    // if (!postTags.contains(tag)) {
    // postTags.add(tag);
    // }
    // }
    post.setTags(tags.subList(0, tagCount));

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
    Set<Comment> comments = IntStream.range(0, count)
        .mapToObj(i -> createFakeComment(posts, users))
        .collect(Collectors.toSet());
    commentRepository.saveAll(comments);
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
    Set<Review> reviews = IntStream.range(0, count)
        .mapToObj(i -> createFakeReview(posts, users))
        .collect(Collectors.toSet());
    reviewRepository.saveAll(reviews);
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

  private String slugify(String text) {
    return text.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .trim();
  }

  private <T> T getRandomElement(List<T> list) {
    return list.get(random.nextInt(list.size()));
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
}
