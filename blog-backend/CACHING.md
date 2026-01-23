# Caching Implementation

This document describes the caching implementation using ConcurrentHashMaps in the blog backend application.

## Overview

The caching system provides thread-safe, in-memory caching with support for:
- Efficient searching using predicates
- Flexible sorting using reflection
- Pagination of cached data
- Automatic cache refresh with configurable TTL (Time-To-Live)
- Cache eviction on data modifications

## Architecture

### Core Components

1. **CacheService Interface** (`cache/CacheService.java`)
   - Generic interface defining cache operations
   - Supports CRUD operations, search, sort, and pagination

2. **ConcurrentMapCache** (`cache/ConcurrentMapCache.java`)
   - Generic implementation using `ConcurrentHashMap`
   - Thread-safe operations for concurrent access
   - Reflection-based field access for sorting
   - Efficient binary search and pagination algorithms

3. **CacheConfig** (`cache/CacheConfig.java`)
   - Spring configuration for cache beans
   - Defines cache instances for different entities:
     - `PostCache`: Caches PostResponse objects (TTL: 5 minutes)
     - `CategoryCache`: Caches Category objects (TTL: 30 minutes)
     - `TagCache`: Caches Tag objects (TTL: 30 minutes)
     - `CommentCache`: Caches Comment objects (TTL: 2 minutes)
   - Scheduled tasks for automatic cache refresh

4. **CacheAspect** (`cache/CacheAspect.java`)
   - AOP-based automatic cache management
   - Intercepts service methods to update cache on:
     - Create operations: Add to cache
     - Update operations: Refresh cache entry
     - Delete operations: Evict from cache

5. **CacheController** (`controllers/CacheController.java`)
   - REST API for cache management (admin only)
   - Endpoints for viewing statistics, clearing, and refreshing caches

## Features

### 1. Efficient Searching

The cache supports predicate-based searching with O(n) complexity:

```java
// Example: Search for published posts
postCache.search(
    post -> post.status() == PostStatus.PUBLISHED,
    page, size, sortField, ascending
);

// Example: Search by keyword
postCache.search(
    post -> post.title().toLowerCase().contains(keyword),
    page, size, sortField, ascending
);
```

**Search Algorithm:**
- Linear scan through cache entries
- Predicate evaluation for each entry
- Results collected and passed to sorting/pagination

### 2. Flexible Sorting

Supports sorting by any field using reflection:

```java
// Sort by createdAt in descending order
cache.paginate(page, size, "createdAt", false);

// Sort by nested fields
cache.paginate(page, size, "author.name", true);
```

**Sorting Algorithm:**
- Uses Java's TimSort (via `Collections.sort`)
- O(n log n) time complexity
- Reflection-based field access with caching
- Supports nested field paths (e.g., "author.name")

### 3. Pagination

Efficient pagination using subList:

```java
// Get page 2 with 20 items per page
PageResponse<PostResponse> response = cache.paginate(1, 20, "createdAt", false);
```

**Pagination Algorithm:**
- O(1) subList extraction
- Calculates total pages and elements
- Returns metadata (first, last, page number, etc.)

### 4. TTL-based Expiration

Each cache entry has a timestamp:
- Automatic expiration based on TTL
- Lazy eviction on access
- Periodic cleanup during scheduled refresh

### 5. Automatic Cache Synchronization

The `CacheAspect` ensures cache consistency:
- **Create**: New entities automatically added to cache
- **Update**: Cache entries updated on modification
- **Delete**: Entries evicted on deletion

## Service Integration

### PostService

```java
@Service
@Slf4j
public class PostService {
  private final PostRepository postRepository;
  private final PostCache postCache;

  // Create - adds to cache
  public PostResponse createPost(CreatePostRequest request, User user) {
    PostResponse response = DtoMapper.toPostResponse(postRepository.save(post));
    postCache.put(response.id(), response);
    return response;
  }

  // Read - checks cache first
  public PostResponse getPostById(Long postId) {
    return postCache.get(postId).orElseGet(() -> {
      var post = postRepository.findById(postId)
          .orElseThrow(() -> BlogException.notFound("Post not found"));
      PostResponse response = DtoMapper.toPostResponse(post);
      postCache.put(postId, response);
      return response;
    });
  }

  // Update - refreshes cache
  public PostResponse updatePost(Long postId, UpdatePostRequest request, Long userId) {
    PostResponse response = DtoMapper.toPostResponse(postRepository.save(post));
    postCache.put(response.id(), response);
    return response;
  }

  // Delete - evicts from cache
  public void deletePost(Long postId, Long userId) {
    postRepository.delete(post);
    postCache.evict(postId);
  }

  // Search with cache
  public PageResponse<PostResponse> getPublishedPosts(Pageable pageable) {
    return postCache.search(
        post -> post.status() == PostStatus.PUBLISHED,
        pageable.getPageNumber(),
        pageable.getPageSize(),
        "createdAt",
        false
    );
  }
}
```

Similar patterns are applied to:
- `CategoryService`
- `TagService`
- `CommentService`

## Cache Management API

### View Cache Statistics

```http
GET /api/cache/stats
Authorization: Bearer <admin-token>
```

Response:
```json
{
  "success": true,
  "message": "Cache statistics retrieved successfully",
  "data": {
    "posts": {
      "name": "PostCache",
      "size": 150,
      "ttlMillis": 300000
    },
    "categories": {
      "name": "CategoryCache",
      "size": 25,
      "ttlMillis": 1800000
    },
    ...
  }
}
```

### Clear Specific Cache

```http
DELETE /api/cache/posts
Authorization: Bearer <admin-token>
```

### Clear All Caches

```http
DELETE /api/cache
Authorization: Bearer <admin-token>
```

### Refresh Cache

```http
POST /api/cache/posts/refresh
Authorization: Bearer <admin-token>
```

### Evict Single Entry

```http
DELETE /api/cache/posts/123
Authorization: Bearer <admin-token>
```

## Configuration

### Cache TTL Settings

Defined in `CacheConfig.java`:

```java
private static final long POST_CACHE_TTL = 5 * 60 * 1000; // 5 minutes
private static final long CATEGORY_CACHE_TTL = 30 * 60 * 1000; // 30 minutes
private static final long TAG_CACHE_TTL = 30 * 60 * 1000; // 30 minutes
private static final long COMMENT_CACHE_TTL = 2 * 60 * 1000; // 2 minutes
```

### Scheduled Refresh

Each cache has a scheduled refresh task:

```java
@Scheduled(fixedRate = POST_CACHE_TTL)
public void refreshPostCache() {
  PostCache cache = postCache();
  cache.clear();
  loadPostCache(cache);
}
```

## Performance Characteristics

| Operation | Time Complexity | Notes |
|-----------|----------------|-------|
| Get | O(1) | HashMap lookup |
| Put | O(1) | HashMap insertion |
| Evict | O(1) | HashMap removal |
| Search | O(n) | Linear scan with predicate |
| Sort | O(n log n) | TimSort algorithm |
| Paginate | O(1) | SubList extraction |
| GetAll | O(n) | Iteration over all entries |

## Thread Safety

- **ConcurrentHashMap**: Provides thread-safe operations without locking the entire map
- **Atomic operations**: Put, get, and evict are atomic
- **Iteration**: Safe for concurrent modifications during iteration
- **Cache timestamps**: Use ConcurrentHashMap for thread-safe timestamp tracking

## Best Practices

1. **Use cache for read-heavy operations**: Caching is most beneficial when reads significantly outnumber writes

2. **Configure appropriate TTL**: Balance between freshness and performance

3. **Monitor cache statistics**: Use the Cache Management API to track cache usage

4. **Handle cache misses gracefully**: Always fall back to database on cache miss

5. **Use predicates efficiently**: Complex predicates may impact search performance

6. **Consider cache warming**: Load frequently accessed data on application startup

## Future Enhancements

1. **Distributed caching**: Use Redis or Hazelcast for multi-instance deployments
2. **Cache metrics**: Integrate with monitoring tools (Prometheus, Grafana)
3. **Intelligent prefetching**: Predict and preload data based on access patterns
4. **Partial cache invalidation**: Invalidate only affected entries on related data changes
5. **Cache size limits**: Implement LRU eviction when cache reaches size threshold
6. **Compression**: Compress large cached objects to reduce memory usage

## Troubleshooting

### Cache Not Updating

- Check if AOP is enabled (`@EnableAspectJAutoProxy`)
- Verify aspect pointcuts match service method signatures
- Check logs for aspect execution

### High Memory Usage

- Reduce TTL values to expire entries more frequently
- Implement cache size limits
- Monitor cache statistics

### Stale Data

- Reduce TTL values
- Manually refresh cache after bulk updates
- Use cache eviction API to clear specific entries

## Example Usage

```java
// Search for posts by user
PageResponse<PostResponse> userPosts = postService.getUserPosts(
    userId, 
    PageRequest.of(0, 10, Sort.by("createdAt").descending())
);

// Search posts by keyword
PageResponse<PostResponse> searchResults = postService.searchPosts(
    "spring boot",
    PageRequest.of(0, 20)
);

// Get category by slug (cached)
Category category = categoryService.getCategoryBySlug("technology");

// Get all tags with pagination
PageResponse<Tag> tags = tagService.getAllTags(
    PageRequest.of(0, 50, Sort.by("name").ascending())
);
```

## Conclusion

This caching implementation provides a robust, efficient, and thread-safe solution for reducing database load and improving application performance. The use of ConcurrentHashMaps ensures excellent concurrent access performance, while the flexible search and pagination APIs make it easy to work with cached data.
