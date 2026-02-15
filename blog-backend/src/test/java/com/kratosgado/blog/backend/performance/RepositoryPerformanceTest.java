package com.kratosgado.blog.backend.performance;

import static org.assertj.core.api.Assertions.assertThat;

import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.models.User;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
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

/**
 * Performance tests for repository queries. Measures and compares query
 * execution times for various
 * operations.
 */
// @SpringBootTest
// @ActiveProfiles("test")
// @Sql(
// scripts = "classpath:db/migration/V2__add_performance_indexes.sql",
// executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
// @Transactional
// @Slf4j
// public class RepositoryPerformanceTest {
// @Autowired private PostRepository postRepository;
// @Autowired private UserRepository userRepository;
// @Autowired private CategoryRepository categoryRepository;
// @Autowired private TagRepository tagRepository;
// @Autowired private QueryPerformanceMonitor performanceMonitor;
// @Autowired private EntityManager entityManager;
//
// private static final int WARMUP_RUNS = 2;
// private static final int MEASURE_RUNS = 5;
//
// private User testUser;
// private Category testCategory;
// private List<Tag> testTags;
//
// @BeforeEach
// public void setup() {
// // Reset metrics before each test
// performanceMonitor.resetMetrics();
//
// // Create test data if not exists
// if (userRepository.count() == 0) {
// testUser = new User();
// testUser.setUsername("perftest_user");
// testUser.setEmail("perftest@example.com");
// testUser.setPassword("password");
// testUser = userRepository.save(testUser);
// } else {
// testUser = userRepository.findAll().get(0);
// }
//
// if (categoryRepository.count() == 0) {
// testCategory = new Category();
// testCategory.setName("Performance Test Category");
// testCategory.setSlug("perf-test-category");
// testCategory = categoryRepository.save(testCategory);
// } else {
// testCategory = categoryRepository.findAll().get(0);
// }
//
// if (tagRepository.count() < 5) {
// testTags = new ArrayList<>();
// for (int i = 0; i < 5; i++) {
// Tag tag = new Tag();
// tag.setName("PerfTag" + i);
// tag.setSlug("perf-tag-" + i);
// testTags.add(tagRepository.save(tag));
// }
// } else {
// testTags = tagRepository.findAll().subList(0, 5);
// }
//
// // Create test posts if not exists
// if (postRepository.count() < 100) {
// createTestPosts(100);
// }
// }
//
// private void createTestPosts(int count) {
// List<Post> posts = new ArrayList<>();
//
// for (int i = 0; i < count; i++) {
// Post post = new Post();
// post.setTitle("Performance Test Post " + i);
// post.setSlug("perf-test-post-" + i);
// post.setContent(
// "This is a performance test post with content for searching. It contains
// keywords like"
// + " java, spring, test, and performance.");
// post.setExcerpt("Performance test excerpt " + i);
// post.setStatus(i % 10 == 0 ? PostStatus.draft : PostStatus.published);
// post.setUser(testUser);
// post.setCategory(testCategory);
// post.setViews(i * 10);
// post.setLikesCount(i * 2);
//
// // Assign tags (rotate through available tags)
// List<Tag> postTags = new ArrayList<>();
// postTags.add(testTags.get(i % testTags.size()));
// if (i % 2 == 0 && testTags.size() > 1) {
// postTags.add(testTags.get((i + 1) % testTags.size()));
// }
// post.setTags(postTags);
//
// posts.add(post);
// }
//
// postRepository.saveAll(posts);
// postRepository.flush();
//
// log.info("Created {} test posts", count);
// }
//
// @Test
// public void testPaginationPerformance() {
// log.info("=== Testing Pagination Performance ===");
//
// int[] pageSizes = {10, 50, 100};
// for (int size : pageSizes) {
// long startTime = System.nanoTime();
// Page<?> page = postRepository.findByStatus(PostStatus.published,
// PageRequest.of(0, size));
// long duration = (System.nanoTime() - startTime) / 1_000_000;
//
// log.info("Page size {}: {}ms, Results: {}", size, duration,
// page.getContent().size());
// assertThat(duration).isLessThan(1000); // Should complete in under 1 second
// }
// }
//
// @Test
// public void testSearchPerformance() {
// log.info("=== Testing Search Performance ===");
//
// String[] searchTerms = {"java", "spring", "test", "performance"};
//
// for (String term : searchTerms) {
// TimingStats likeStats =
// measureSearch(
// () -> postRepository.searchPublishedPostsSimple(term, PageRequest.of(0,
// 20)));
// TimingStats ftsStats =
// measureSearch(() -> postRepository.searchPublishedPosts(term,
// PageRequest.of(0, 20)));
//
// double improvement = likeStats.averageMillis() / ftsStats.averageMillis();
// log.info(
// "Search '{}': LIKE avg {}ms, FTS avg {}ms ({}x)",
// term,
// String.format("%.2f", likeStats.averageMillis()),
// String.format("%.2f", ftsStats.averageMillis()),
// String.format("%.2f", improvement));
// if (likeStats.resultCount() == 0 || ftsStats.resultCount() == 0) {
// log.warn("Search '{}' returned no results; verify test data and search
// config.", term);
// }
// assertThat(ftsStats.averageMillis()).isLessThan(5000);
// }
// }
//
// @Test
// public void testFilteringPerformance() {
// log.info("=== Testing Filtering Performance ===");
//
// // Test filtering by user
// long startTime = System.nanoTime();
// Page<?> userPosts = postRepository.findByUserId(testUser.getId(),
// PageRequest.of(0, 10));
// long userDuration = (System.nanoTime() - startTime) / 1_000_000;
// log.info("Filter by user: {}ms, Results: {}", userDuration,
// userPosts.getTotalElements());
//
// // Test filtering by category
// startTime = System.nanoTime();
// Page<?> categoryPosts =
// postRepository.findByCategoryId(testCategory.getId(), PageRequest.of(0, 10));
// long categoryDuration = (System.nanoTime() - startTime) / 1_000_000;
// log.info(
// "Filter by category: {}ms, Results: {}",
// categoryDuration,
// categoryPosts.getTotalElements());
//
// // Test filtering by status
// startTime = System.nanoTime();
// Page<?> statusPosts = postRepository.findByStatus(PostStatus.published,
// PageRequest.of(0, 10));
// long statusDuration = (System.nanoTime() - startTime) / 1_000_000;
// log.info("Filter by status: {}ms, Results: {}", statusDuration,
// statusPosts.getTotalElements());
//
// assertThat(userDuration).isLessThan(500);
// assertThat(categoryDuration).isLessThan(500);
// assertThat(statusDuration).isLessThan(500);
// }
//
// @Test
// public void testSortingPerformance() {
// log.info("=== Testing Sorting Performance ===");
//
// String[] sortFields = {"createdAt", "views", "title"};
// for (String field : sortFields) {
// long startTime = System.nanoTime();
// Page<?> results =
// postRepository.findByStatus(
// PostStatus.published,
// PageRequest.of(0, 20,
// org.springframework.data.domain.Sort.by(field).descending()));
// long duration = (System.nanoTime() - startTime) / 1_000_000;
//
// log.info("Sort by {}: {}ms, Results: {}", field, duration,
// results.getTotalElements());
// assertThat(duration).isLessThan(1000);
// }
// }
//
// @Test
// public void testComplexQueryPerformance() {
// log.info("=== Testing Complex Query Performance ===");
//
// // Test query with entity graph (eager loading)
// long startTime = System.nanoTime();
// var postDetails = postRepository.findByStatus(PostStatus.published,
// PageRequest.of(0, 10));
// long duration = (System.nanoTime() - startTime) / 1_000_000;
//
// log.info(
// "Complex query with entity graph: {}ms, Results: {}",
// duration,
// postDetails.getTotalElements());
// assertThat(duration).isLessThan(1000);
// }
//
// @Test
// public void testAggregationPerformance() {
// log.info("=== Testing Aggregation Performance ===");
//
// // Test count aggregation
// long startTime = System.nanoTime();
// long count = postRepository.countByStatus(PostStatus.published);
// long countDuration = (System.nanoTime() - startTime) / 1_000_000;
// log.info("Count by status: {}ms, Result: {}", countDuration, count);
//
// // Test sum aggregation
// startTime = System.nanoTime();
// long viewsSum = postRepository.sumViewsByUserId(testUser.getId());
// long sumDuration = (System.nanoTime() - startTime) / 1_000_000;
// log.info("Sum views by user: {}ms, Result: {}", sumDuration, viewsSum);
//
// assertThat(countDuration).isLessThan(500);
// assertThat(sumDuration).isLessThan(500);
// }
//
// @Test
// public void testTopNQueriesPerformance() {
// log.info("=== Testing Top N Queries Performance ===");
//
// int[] limits = {5, 10, 20};
// for (int limit : limits) {
// // Test top by views
// long startTime = System.nanoTime();
// List<?> topByViews = postRepository.findTopNByOrderByViewsDesc(limit);
// long viewsDuration = (System.nanoTime() - startTime) / 1_000_000;
// log.info("Top {} by views: {}ms, Results: {}", limit, viewsDuration,
// topByViews.size());
//
// // Test top by created date
// startTime = System.nanoTime();
// List<?> topByDate = postRepository.findTopNByOrderByCreatedAtDesc(limit);
// long dateDuration = (System.nanoTime() - startTime) / 1_000_000;
// log.info("Top {} by date: {}ms, Results: {}", limit, dateDuration,
// topByDate.size());
//
// assertThat(viewsDuration).isLessThan(500);
// assertThat(dateDuration).isLessThan(500);
// }
// }
//
// @Test
// public void printPerformanceReport() {
// // Run all operations
// testPaginationPerformance();
// testSearchPerformance();
// testFilteringPerformance();
// testSortingPerformance();
//
// // Print comprehensive report
// performanceMonitor.printReport();
// }
//
// private TimingStats measureSearch(Supplier<Page<?>> search) {
// for (int i = 0; i < WARMUP_RUNS; i++) {
// clearPersistenceContext();
// search.get();
// }
//
// long total = 0;
// long min = Long.MAX_VALUE;
// long max = 0;
// long resultCount = 0;
// for (int i = 0; i < MEASURE_RUNS; i++) {
// clearPersistenceContext();
// long start = System.nanoTime();
// Page<?> page = search.get();
// long duration = System.nanoTime() - start;
// total += duration;
// min = Math.min(min, duration);
// max = Math.max(max, duration);
// resultCount = Math.max(resultCount, page.getContent().size());
// }
//
// return new TimingStats(
// toMillis(total / (double) MEASURE_RUNS), toMillis(min), toMillis(max),
// resultCount);
// }
//
// private void clearPersistenceContext() {
// entityManager.flush();
// entityManager.clear();
// entityManager.getEntityManagerFactory().getCache().evictAll();
// }
//
// private double toMillis(double nanos) {
// return nanos / 1_000_000.0;
// }
//
// private record TimingStats(
// double averageMillis, double minMillis, double maxMillis, long resultCount)
// {}
// }
