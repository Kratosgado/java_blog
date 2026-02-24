package com.kratosgado.blog.backend.seeders;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.kratosgado.blog.backend.models.Category;
import com.kratosgado.blog.backend.models.Comment;
import com.kratosgado.blog.backend.models.Post;
import com.kratosgado.blog.backend.models.Review;
import com.kratosgado.blog.backend.models.Tag;
import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.repositories.mongo.ReviewRepository;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.enums.UserRole;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class FakeDataSeeder implements CommandLineRunner {
  private final JdbcTemplate jdbcTemplate; // Add to constructor

  private final Faker faker = new Faker();
  private final Random random = new Random();
  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;

  @Override
  public void run(String... args) {
    log.info("Starting data seeding...");

    // Check if data already exists
    if (!reviewRepository.findAll().isEmpty()) {
      log.info("Database already contains data. Skipping seeding.");
      return;
    }
    ensureSearchVectorExists();
    clearDatabase();
    performDataSeeding();
    log.info("Data seeding completed successfully!");
  }

  @Transactional(isolation = Isolation.SERIALIZABLE)
  private void clearDatabase() {
    log.info("Clearing database...");
    jdbcTemplate.execute(
        "TRUNCATE TABLE post_tags, posts, categories, users RESTART IDENTITY CASCADE");
    commentRepository.deleteAll();
    reviewRepository.deleteAll();
    log.info("Database cleared successfully!");
  }

  @Transactional
  private void performDataSeeding() {
    log.info("Performing data seeding...");

    // Seed in order: Users -> Categories -> Tags -> Posts -> Comments -> Reviews
    List<User> users = seedUsers(20);
    log.info("Seeded {} users", users.size());

    List<Category> categories = seedCategories(10);
    log.info("Seeded {} categories", categories.size());

    List<Tag> tags = seedTags(6);
    log.info("Seeded {} tags", tags.size());

    List<Post> posts = seedPosts(100000, users, categories, tags);
    log.info("Seeded {} posts", posts.size());

    int commentCount = seedComments(500, posts, users);
    log.info("Seeded {} comments", commentCount);

    int reviewCount = seedReviews(200, posts, users);
    log.info("Seeded {} reviews", reviewCount);

    log.info("Data seeding completed successfully!");
  }

  private List<User> seedUsers(int count) {
    List<User> users =
        IntStream.range(0, count).mapToObj(i -> createFakeUser(i)).collect(Collectors.toList());
    users.addAll(
        List.of(
            createRealUser("gado", UserRole.ADMIN),
            createRealUser("kratos", UserRole.READER),
            createRealUser("kratosgado", UserRole.AUTHOR)));

    return userRepository.saveAll(users);
  }

  private User createRealUser(String username, UserRole role) {
    return User.builder()
        .username(username)
        .email(username + "@gmail.com")
        .password(BCrypt.withDefaults().hashToString(12, "28935617Aa@".toCharArray()))
        .avatarUrl("https://avatars.githubusercontent.com/u/10137?v=4")
        .role(role)
        .build();
  }

  private User createFakeUser(int index) {
    String firstName = faker.name().firstName();
    String lastName = faker.name().lastName();
    String username = (firstName + lastName + index).toLowerCase();

    return User.builder()
        .username(username)
        .email(faker.internet().emailAddress(username))
        .password(BCrypt.withDefaults().hashToString(12, "@Password123@".toCharArray()))
        .avatarUrl(faker.avatar().image())
        .role(faker.options().option(UserRole.class)) // First user is admin
        .build();
  }

  private List<Category> seedCategories(int count) {
    List<String> categoryNames =
        List.of(
            "Technology",
            "Travel",
            "Food & Cooking",
            "Health & Fitness",
            "Business",
            "Lifestyle",
            "Entertainment",
            "Sports",
            "Science",
            "Education",
            "Art & Design",
            "Photography",
            "Music",
            "Fashion",
            "Finance",
            "Gaming");

    List<Category> categories =
        categoryNames.stream()
            .limit(count)
            .map(this::createFakeCategory)
            .collect(Collectors.toList());

    return categories.stream()
        .map(category -> categoryRepository.save(category))
        .collect(Collectors.toList());
  }

  private Category createFakeCategory(String name) {
    return Category.builder()
        .name(name)
        .slug(BlogUtils.toSlug(name))
        .description(faker.lorem().sentence(20))
        .build();
  }

  private List<Tag> seedTags(int count) {
    List<Tag> tags =
        IntStream.range(0, count).mapToObj(i -> createFakeTag()).collect(Collectors.toList());

    return tags.stream().map(tag -> tagRepository.save(tag)).collect(Collectors.toList());
  }

  private Tag createFakeTag() {
    String name =
        faker
            .options()
            .option(
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

  private List<Post> seedPosts(
      int count, List<User> users, List<Category> categories, List<Tag> tags) {
    List<Post> posts =
        IntStream.range(0, count)
            .mapToObj(i -> createFakePost(users, categories, tags))
            .collect(Collectors.toList());

    return postRepository.saveAll(posts);
  }

  private Post createFakePost(List<User> users, List<Category> categories, List<Tag> tags) {
    Post post = new Post();

    // Basic fields
    String title = faker.book().title() + " " + faker.lorem().sentence();
    post.setTitle(title);
    post.setSlug(BlogUtils.toSlug(title));
    post.setContent(generateBlogContent());
    post.setExcerpt(faker.lorem().sentence(20));
    post.setStatus(faker.options().option(PostStatus.class));
    post.setViews(faker.number().numberBetween(0, 5000));
    post.setLikesCount(faker.number().numberBetween(0, 1000));
    post.setCoverImage(faker.internet().image());

    // Relationships
    post.setUser(getRandomElement(users));
    post.setCategory(getRandomElement(categories));
    var postTags = new ArrayList<>();
    int tagCount = faker.number().numberBetween(2, Math.min(6, tags.size() + 1));
    for (int i = 0; i < tagCount && i < tags.size(); i++) {
      postTags.add(tags.get(i));
    }
    return post;
  }

  private String generateBlogContent() {
    int paragraphCount = faker.number().numberBetween(5, 15);
    return IntStream.range(0, paragraphCount)
        .mapToObj(
            i -> {
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
    List<Comment> comments =
        IntStream.range(0, count)
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
    comment.setStatus(
        faker
            .options()
            .option(
                CommentStatus.approved,
                CommentStatus.approved,
                CommentStatus.approved,
                CommentStatus.pending)); // 75% approved, 25% pending
    comment.setCreatedAt(generateRandomPastDate());
    comment.setUpdatedAt(comment.getCreatedAt());

    return comment;
  }

  private int seedReviews(int count, List<Post> posts, List<User> users) {
    List<Review> reviews =
        IntStream.range(0, count)
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
    return LocalDateTime.now().minusDays(daysAgo).minusHours(hoursAgo).minusMinutes(minutesAgo);
  }

  private void ensureSearchVectorExists() {
    log.info("Ensuring search_vector column and GIN index exist...");
    jdbcTemplate.execute(
"""
            CREATE INDEX IF NOT EXISTS idx_posts_status_views
ON posts(status, views DESC);

CREATE INDEX IF NOT EXISTS idx_posts_user_status
ON posts(user_id, status);

CREATE INDEX IF NOT EXISTS idx_posts_category_status
ON posts(category_id, status);

ALTER TABLE posts DROP COLUMN search_vector;

ALTER TABLE posts
ADD COLUMN  search_vector tsvector
GENERATED ALWAYS AS (
    setweight(to_tsvector('english', title), 'A') ||
    setweight(to_tsvector('english', COALESCE(content, '')), 'B')
) STORED;
-- Create the GIN index on the stored column

CREATE INDEX IF NOT EXISTS idx_posts_published_created_at
ON posts(created_at DESC) WHERE status = 'published';

CREATE INDEX IF NOT EXISTS idx_posts_fts_vector
ON posts USING GIN(search_vector);


ANALYZE posts;
ANALYZE post_tags;
ANALYZE users;
ANALYZE categories;
ANALYZE tags;
""");
  }
}
