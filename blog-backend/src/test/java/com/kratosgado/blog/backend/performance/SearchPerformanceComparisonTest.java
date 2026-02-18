package com.kratosgado.blog.backend.performance;

import static org.assertj.core.api.Assertions.assertThat;

import com.kratosgado.blog.backend.models.Category;
import com.kratosgado.blog.backend.models.Post;
import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.response.PostResponse.PostView;
import com.kratosgado.blog.enums.PostStatus;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@Sql(
    scripts = "classpath:db/migration/V2__add_performance_indexes.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class SearchPerformanceComparisonTest {

  @Autowired private PostRepository postRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private CategoryRepository categoryRepository;
  @Autowired private TagRepository tagRepository;
  @Autowired private EntityManager entityManager;

  private static final int POST_COUNT = 100_000; // Large enough to make Seq Scan expensive
  private static final int WARMUP_RUNS = 3;
  private static final int MEASURE_RUNS = 8;

  @BeforeEach
  public void setup() {
    ensurePerformanceIndexes();
    if (postRepository.count() >= POST_COUNT) {
      log.info("Test data already exists, skipping generation.");
      return;
    }

    // Clear existing data to ensure clean state
    postRepository.deleteAll();
    userRepository.deleteAll();
    categoryRepository.deleteAll();
    tagRepository.deleteAll();

    createTestData();

    // Update statistics after data generation
    log.info("Analyzing table to update statistics...");
    entityManager.createNativeQuery("ANALYZE posts").executeUpdate();
  }

  @Test
  @Transactional
  public void compareSearchPerformance() {
    String keyword = "java";

    log.info("=== Search Performance Comparison ({} posts) ===", POST_COUNT);

    TimingStats legacyStats = measureSearch("Legacy LIKE", () -> performLegacySearch(keyword));
    TimingStats optimizedStats = measureSearch("TSVector", () -> performOptimizedSearch(keyword));

    log.info(
        "Legacy Search Avg/Min/Max: {}/{}/{} ms",
        String.format("%.2f", legacyStats.averageMillis()),
        String.format("%.2f", legacyStats.minMillis()),
        String.format("%.2f", legacyStats.maxMillis()));
    log.info(
        "Optimized Search Avg/Min/Max: {}/{}/{} ms",
        String.format("%.2f", optimizedStats.averageMillis()),
        String.format("%.2f", optimizedStats.minMillis()),
        String.format("%.2f", optimizedStats.maxMillis()));

    double improvement = legacyStats.averageMillis() / optimizedStats.averageMillis();
    log.info("Performance Improvement: {}x faster", String.format("%.2f", improvement));

    explainQueries(keyword);

    assertThat(legacyStats.resultCount()).isGreaterThan(0);
    assertThat(optimizedStats.resultCount()).isGreaterThan(0);
    assertThat(optimizedStats.averageMillis()).isLessThan(5000);
  }

  private void createTestData() {
    log.info("Generating {} test posts...", POST_COUNT);
    User user = new User();
    user.setUsername("perf_tester");
    user.setEmail("perf@test.com");
    user.setPassword("password");
    user = userRepository.save(user);

    Category category = new Category();
    category.setName("Performance");
    category.setSlug("performance");
    category = categoryRepository.save(category);

    List<Post> batch = new ArrayList<>();
    Random random = new Random(42);
    String[] keywords = {
      "java", "spring", "performance", "database", "optimization", "index", "query", "search"
    };

    for (int i = 0; i < POST_COUNT; i++) {
      Post post = new Post();
      // Make "java" appear in only about 0.1% of posts (100 in 100,000)
      boolean shouldMatch = (i % 1000 == 0);
      String currentKeyword = shouldMatch ? "java" : keywords[random.nextInt(keywords.length)];
      // If it shouldn't match, ensure currentKeyword is NOT java
      if (!shouldMatch && "java".equals(currentKeyword)) {
        currentKeyword = "software";
      }

      String title = "Post " + i + " about " + currentKeyword;
      post.setTitle(title);
      post.setSlug("post-" + i);
      // Create a reasonably long content to make FTS worthwhile
      StringBuilder content = new StringBuilder();
      for (int j = 0; j < 20; j++) {
        content.append("This is paragraph ").append(j).append(" discussing ");
        // Ensure "java" doesn't appear in content for non-matching posts
        String randomKeyword = keywords[random.nextInt(keywords.length)];
        if (!shouldMatch && "java".equals(randomKeyword)) {
          randomKeyword = "engineering";
        }
        content.append(randomKeyword).append(" ");
        content.append("and other topics related to software engineering. ");
      }
      post.setContent(content.toString());
      post.setExcerpt("Excerpt for post " + i);
      post.setStatus(PostStatus.published);
      post.setUser(user);
      post.setCategory(category);
      post.setViews(random.nextInt(1000));
      post.setCreatedAt(java.time.LocalDateTime.now().minusDays(random.nextInt(365)));

      batch.add(post);
      if (batch.size() >= 500) { // Larger batch for 100k rows
        postRepository.saveAll(batch);
        batch.clear();
        if (i % 5000 == 0) log.info("Generated {} posts...", i);
      }
    }
    if (!batch.isEmpty()) {
      postRepository.saveAll(batch);
    }
    log.info("Test data generation complete.");
  }

  private Page<PostView> performLegacySearch(String keyword) {
    return postRepository.searchPublishedPostsSimple(keyword, PageRequest.of(0, 20));
  }

  private Page<PostView> performOptimizedSearch(String keyword) {
    // Use the repository method which uses the native query with tsvector
    return postRepository.searchPublishedPosts(keyword, PageRequest.of(0, 20));
  }

  private void explainQueries(String keyword) {
    log.info("=== Query Execution Plans ===");

    String legacyExplain =
        "EXPLAIN (ANALYZE, BUFFERS, TIMING) SELECT * FROM posts WHERE status = 'published' AND "
            + "(LOWER(title) LIKE LOWER(?) OR LOWER(content) LIKE LOWER(?)) LIMIT 20";
    List<String> legacyPlan =
        entityManager
            .createNativeQuery(legacyExplain)
            .setParameter(1, "%" + keyword + "%")
            .setParameter(2, "%" + keyword + "%")
            .getResultList();
    log.info("Legacy Search Plan:\n{}", String.join("\n", legacyPlan));

    String optimizedExplain =
        "EXPLAIN (ANALYZE, BUFFERS, TIMING) SELECT * FROM posts "
            + "WHERE status = 'published' AND search_vector @@ websearch_to_tsquery('english', ?) "
            + "ORDER BY ts_rank(search_vector, websearch_to_tsquery('english', ?)) DESC LIMIT 20";
    List<String> optimizedPlan =
        entityManager
            .createNativeQuery(optimizedExplain)
            .setParameter(1, keyword)
            .setParameter(2, keyword)
            .getResultList();
    log.info("Optimized Search Plan:\n{}", String.join("\n", optimizedPlan));
  }

  private void ensurePerformanceIndexes() {
    entityManager
        .createNativeQuery(
            "CREATE INDEX IF NOT EXISTS idx_posts_status_views ON posts(status, views DESC)")
        .executeUpdate();
    entityManager
        .createNativeQuery(
            "CREATE INDEX IF NOT EXISTS idx_posts_user_status ON posts(user_id, status)")
        .executeUpdate();
    entityManager
        .createNativeQuery(
            "CREATE INDEX IF NOT EXISTS idx_posts_category_status ON posts(category_id, status)")
        .executeUpdate();
    entityManager
        .createNativeQuery(
            "CREATE INDEX IF NOT EXISTS idx_posts_fts_vector ON posts USING GIN(search_vector)")
        .executeUpdate();
    entityManager
        .createNativeQuery(
            "CREATE INDEX IF NOT EXISTS idx_posts_published_created_at "
                + "ON posts(created_at DESC) WHERE status = 'published'")
        .executeUpdate();
  }

  private TimingStats measureSearch(String label, Supplier<Page<PostView>> search) {
    for (int i = 0; i < WARMUP_RUNS; i++) {
      clearPersistenceContext();
      search.get();
    }

    long total = 0;
    long min = Long.MAX_VALUE;
    long max = 0;
    long resultCount = 0;
    for (int i = 0; i < MEASURE_RUNS; i++) {
      clearPersistenceContext();
      long start = System.nanoTime();
      Page<PostView> page = search.get();
      long duration = System.nanoTime() - start;
      total += duration;
      min = Math.min(min, duration);
      max = Math.max(max, duration);
      resultCount = Math.max(resultCount, page.getTotalElements());
    }

    TimingStats stats =
        new TimingStats(
            toMillis(total / (double) MEASURE_RUNS), toMillis(min), toMillis(max), resultCount);
    log.info(
        "{} Avg/Min/Max: {}/{}/{} ms (results: {})",
        label,
        String.format("%.2f", stats.averageMillis()),
        String.format("%.2f", stats.minMillis()),
        String.format("%.2f", stats.maxMillis()),
        stats.resultCount());
    return stats;
  }

  private void clearPersistenceContext() {
    entityManager.flush();
    entityManager.clear();
    entityManager.getEntityManagerFactory().getCache().evictAll();
  }

  private double toMillis(double nanos) {
    return nanos / 1_000_000.0;
  }

  private record TimingStats(
      double averageMillis, double minMillis, double maxMillis, long resultCount) {}
}
