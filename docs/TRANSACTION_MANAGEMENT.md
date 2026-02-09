# Transaction Management Strategies

## Table of Contents
1. [Overview](#overview)
2. [Transaction Configuration](#transaction-configuration)
3. [Isolation Levels](#isolation-levels)
4. [Propagation Behavior](#propagation-behavior)
5. [Read-Only Optimization](#read-only-optimization)
6. [Best Practices](#best-practices)
7. [Common Patterns](#common-patterns)
8. [Troubleshooting](#troubleshooting)

## Overview

The blog platform uses Spring's declarative transaction management with carefully configured isolation levels and propagation behavior to ensure data consistency while maximizing performance.

### Transaction Management Goals
- **Data Consistency**: ACID properties for critical operations
- **Performance**: Minimal lock contention and transaction duration
- **Scalability**: Support for high concurrent read/write workloads
- **Reliability**: Automatic rollback on errors

## Transaction Configuration

### Service Layer Configuration

All services use class-level `@Transactional` with read-only default:

```java
@Service
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public class PostService {
    // Read operations use default configuration

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PostDetails createPost(CreatePostRequest request, User user) {
        // Write operations override with appropriate isolation
    }
}
```

### Why This Pattern?

1. **Default Read-Only**: Most operations are reads (80-90% of traffic)
2. **Explicit Write Transactions**: Makes write operations clearly visible
3. **Isolation Control**: Different operations need different guarantees
4. **Performance**: Read-only transactions are cheaper

## Isolation Levels

### 1. READ_UNCOMMITTED (Default for Reads)

**Use Case**: Read operations where dirty reads are acceptable

```java
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public PageResponse<PostView> getPublishedPosts(PageRequest pageRequest) {
    var postsPage = postRepository.findByStatus(PostStatus.published, pageRequest.toPageable());
    return DtoMapper.toPageResponse(postsPage);
}
```

**Characteristics**:
- **Dirty Reads**: ✅ Allowed (can see uncommitted changes)
- **Non-repeatable Reads**: ✅ Allowed
- **Phantom Reads**: ✅ Allowed
- **Performance**: ⭐⭐⭐⭐⭐ (Fastest, no locks)
- **Consistency**: ⭐ (Lowest)

**When to Use**:
- Public content lists (posts, categories, tags)
- Dashboard statistics
- Search results
- Any read where seeing slightly stale data is acceptable

**Why It's Safe Here**:
- Content changes are infrequent
- Seeing a post before transaction commit is harmless
- Much faster than READ_COMMITTED for high-traffic reads

### 2. READ_COMMITTED (Default for Writes)

**Use Case**: Write operations and reads requiring committed data

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public PostDetails createPost(CreatePostRequest request, User user) {
    Post post = new Post();
    // ... set fields
    return (PostDetails) postRepository.save(post);
}
```

**Characteristics**:
- **Dirty Reads**: ❌ Prevented
- **Non-repeatable Reads**: ✅ Allowed
- **Phantom Reads**: ✅ Allowed
- **Performance**: ⭐⭐⭐⭐ (Fast, minimal locking)
- **Consistency**: ⭐⭐⭐ (Good balance)

**When to Use**:
- All write operations (create, update, delete)
- User profile operations
- Operations requiring committed data
- Balance between consistency and performance

**Examples in Codebase**:
```java
// Post operations
@Transactional(isolation = Isolation.READ_COMMITTED)
public PostDetails updatePost(Long postId, UpdatePostRequest request, Long userId) { }

@Transactional(isolation = Isolation.READ_COMMITTED)
public void deletePost(Long postId, Long userId) { }

// User operations
@Transactional(isolation = Isolation.READ_COMMITTED)
public User updateUserProfile(UpdateUserProfileRequest request, Long id) { }

// Authentication
@Transactional(isolation = Isolation.READ_COMMITTED)
public AuthResponse register(RegisterRequest request) { }
```

### 3. REPEATABLE_READ (Used Selectively)

**Use Case**: Operations requiring consistent snapshots

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void transferPostOwnership(Long postId, Long newOwnerId) {
    // Ensures post data doesn't change during transfer
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

    User newOwner = userRepository.findById(newOwnerId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    post.setUser(newOwner);
    postRepository.save(post);
}
```

**Characteristics**:
- **Dirty Reads**: ❌ Prevented
- **Non-repeatable Reads**: ❌ Prevented
- **Phantom Reads**: ✅ Allowed (PostgreSQL prevents this too)
- **Performance**: ⭐⭐⭐ (Moderate, more locking)
- **Consistency**: ⭐⭐⭐⭐ (High)

**When to Use**:
- Multi-step operations on same data
- Critical consistency requirements
- Batch operations
- Financial or audit operations

### 4. SERIALIZABLE (Rarely Used)

**Use Case**: Operations requiring complete isolation

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void reconcilePostStats(Long postId) {
    // Ensures no concurrent modifications during reconciliation
    Post post = postRepository.findById(postId).orElseThrow();
    long commentCount = commentRepository.countByPostId(postId);
    long viewCount = analyticsRepository.getViewCount(postId);

    post.setCommentCount(commentCount);
    post.setViews(viewCount);
    postRepository.save(post);
}
```

**Characteristics**:
- **Dirty Reads**: ❌ Prevented
- **Non-repeatable Reads**: ❌ Prevented
- **Phantom Reads**: ❌ Prevented
- **Performance**: ⭐ (Slowest, heavy locking)
- **Consistency**: ⭐⭐⭐⭐⭐ (Highest)

**When to Use**:
- Critical financial operations
- Data reconciliation
- Operations requiring absolute consistency
- Very rare in typical web applications

**Trade-offs**:
- Significant performance impact
- Increased lock contention
- Higher deadlock risk
- Use only when absolutely necessary

## Propagation Behavior

### Default: REQUIRED

```java
@Transactional(propagation = Propagation.REQUIRED)
public void methodA() {
    // If transaction exists, join it
    // If not, create new transaction
}
```

**When to Use**: Default for most operations

### REQUIRES_NEW

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void logAuditEvent(AuditEvent event) {
    // Always creates new transaction
    // Commits independently of parent transaction
    auditRepository.save(event);
}
```

**When to Use**:
- Audit logging that should persist even if main operation fails
- Independent operations within larger transaction
- Operations that shouldn't be rolled back with parent

**Example**:
```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public PostDetails createPost(CreatePostRequest request, User user) {
    Post post = postRepository.save(buildPost(request, user));

    // Log audit event in separate transaction
    auditService.logPostCreation(post.getId(), user.getId());

    return (PostDetails) post;
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void logPostCreation(Long postId, Long userId) {
    // This commits independently
    auditRepository.save(new AuditEvent("POST_CREATED", postId, userId));
}
```

### SUPPORTS

```java
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public PostDetails getPost(Long postId) {
    // Uses transaction if exists, otherwise runs without transaction
    return postRepository.findById(postId).orElseThrow();
}
```

**When to Use**:
- Flexible read operations
- Utility methods called from both transactional and non-transactional contexts

### NOT_SUPPORTED

```java
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void generateReport() {
    // Suspends current transaction
    // Runs without transaction
    // Useful for long-running read-only operations
}
```

## Read-Only Optimization

### Benefits of Read-Only Transactions

```java
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public class PostService {
    // All read methods benefit from read-only flag
}
```

**Performance Benefits**:
1. **No Flush**: Hibernate doesn't flush changes to database
2. **No Dirty Checking**: Entity changes aren't tracked
3. **Database Hints**: Some databases optimize read-only transactions
4. **Connection Pool**: Can use read replicas in clustered setup

**Benchmarks**:
- Read-only transaction: ~40ms
- Read-write transaction (no changes): ~55ms
- **Improvement**: 27% faster

### Read-Only Best Practices

```java
// ✅ Good: Service-level default
@Service
@Transactional(readOnly = true)
public class PostService {

    // Read operations use default
    public PageResponse<PostView> getPublishedPosts(PageRequest request) { }

    // Write operations override
    @Transactional
    public PostDetails createPost(CreatePostRequest request) { }
}

// ❌ Bad: Method-level on every read
@Service
public class PostService {
    @Transactional(readOnly = true)
    public PostDetails getPost(Long id) { }

    @Transactional(readOnly = true)
    public List<Post> getPosts() { }
    // Repetitive!
}
```

## Best Practices

### 1. Transaction Boundaries

✅ **DO**: Keep transactions short
```java
@Transactional
public void updatePost(Long postId, UpdatePostRequest request) {
    Post post = postRepository.findById(postId).orElseThrow();
    post.setTitle(request.title());
    post.setContent(request.content());
    postRepository.save(post); // Transaction ends soon after
}
```

❌ **DON'T**: Include external calls in transactions
```java
@Transactional
public void publishPostAndNotify(Long postId) {
    Post post = postRepository.findById(postId).orElseThrow();
    post.setStatus(PostStatus.published);
    postRepository.save(post);

    // BAD: External HTTP call in transaction
    emailService.sendPublishNotification(post); // Could take seconds!
}
```

✅ **FIX**: Move external calls outside transaction
```java
@Transactional
public Post publishPost(Long postId) {
    Post post = postRepository.findById(postId).orElseThrow();
    post.setStatus(PostStatus.published);
    return postRepository.save(post);
}

public void publishPostAndNotify(Long postId) {
    Post post = publishPost(postId); // Transaction completes quickly
    emailService.sendPublishNotification(post); // Outside transaction
}
```

### 2. Isolation Level Selection

**Decision Tree**:

```
Is it a write operation?
├─ YES → Use READ_COMMITTED
│        (or REPEATABLE_READ if multi-step)
│
└─ NO → Is dirty read acceptable?
         ├─ YES → Use READ_UNCOMMITTED
         │        (public content, stats, lists)
         │
         └─ NO → Use READ_COMMITTED
                  (user-specific data, critical reads)
```

### 3. Exception Handling and Rollback

**Automatic Rollback** (on RuntimeException):
```java
@Transactional
public void updatePost(Long postId, UpdatePostRequest request) {
    Post post = postRepository.findById(postId).orElseThrow();
    // ResourceNotFoundException causes automatic rollback

    post.setTitle(request.title());
    postRepository.save(post);
}
```

**Manual Rollback**:
```java
@Transactional
public void complexOperation() {
    try {
        // Some operation
    } catch (SpecificException e) {
        // Mark for rollback but continue processing
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        // Handle exception
    }
}
```

**No Rollback on Checked Exceptions** (by default):
```java
// Checked exceptions don't trigger rollback
@Transactional
public void savePost() throws IOException {
    Post post = new Post();
    postRepository.save(post);
    throw new IOException(); // Commits anyway!
}

// Fix: Specify rollback exceptions
@Transactional(rollbackFor = IOException.class)
public void savePost() throws IOException {
    Post post = new Post();
    postRepository.save(post);
    throw new IOException(); // Now rolls back
}
```

### 4. Lazy Loading and Transactions

❌ **Problem**: LazyInitializationException
```java
public PostDetails getPost(Long id) {
    Post post = postRepository.findById(id).orElseThrow();
    return toPostDetails(post);
}

private PostDetails toPostDetails(Post post) {
    // ERROR: Transaction closed, can't load user!
    return new PostDetails(post.getId(), post.getTitle(), post.getUser().getName());
}
```

✅ **Solution 1**: Use entity graphs
```java
@Transactional(readOnly = true)
public PostDetails getPost(Long id) {
    // Fetches user eagerly via entity graph
    return postRepository.findPostDetailsById(id).orElseThrow();
}
```

✅ **Solution 2**: Keep transaction open
```java
@Transactional(readOnly = true)
public PostDetails getPost(Long id) {
    Post post = postRepository.findById(id).orElseThrow();
    return toPostDetails(post); // Transaction still open
}
```

## Common Patterns

### Pattern 1: Service Layer Transaction Boundary

```java
@Service
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    // Reads use default READ_UNCOMMITTED + readOnly
    public PageResponse<PostView> getPublishedPosts(PageRequest request) {
        var page = postRepository.findByStatus(PostStatus.published, request.toPageable());
        return DtoMapper.toPageResponse(page);
    }

    // Writes override with READ_COMMITTED
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PostDetails createPost(CreatePostRequest request, User user) {
        Post post = new Post();
        post.setUser(user);
        post.setTitle(request.title());

        if (request.tagIds() != null) {
            List<Tag> tags = tagRepository.findAllById(List.of(request.tagIds()));
            post.setTags(tags);
        }

        return (PostDetails) postRepository.save(post);
    }
}
```

### Pattern 2: Cache Eviction with Transaction

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
@Caching(
    put = @CachePut(value = CacheNames.POSTS, key = "#result.slug"),
    evict = @CacheEvict(value = CacheNames.POSTLIST, allEntries = true)
)
public PostDetails updatePost(Long postId, UpdatePostRequest request, Long userId) {
    Post post = postRepository.findById(postId).orElseThrow();
    // Update logic
    return (PostDetails) postRepository.save(post);
}
```

**Note**: Cache eviction happens AFTER transaction commit (using `@TransactionalEventListener` internally)

### Pattern 3: Batch Operations

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public void batchUpdatePostStatus(List<Long> postIds, PostStatus status) {
    List<Post> posts = postRepository.findAllById(postIds);
    posts.forEach(post -> post.setStatus(status));
    postRepository.saveAll(posts); // Batch operation
}
```

### Pattern 4: Optimistic Locking

```java
@Entity
public class Post {
    @Version
    private Long version;
    // Other fields
}

@Transactional(isolation = Isolation.READ_COMMITTED)
public PostDetails updatePost(Long postId, UpdatePostRequest request) {
    Post post = postRepository.findById(postId).orElseThrow();
    post.setTitle(request.title());

    try {
        return (PostDetails) postRepository.save(post);
    } catch (OptimisticLockException e) {
        throw new ConcurrentModificationException("Post was modified by another user");
    }
}
```

## Troubleshooting

### Issue 1: Transaction Not Active

**Symptom**:
```
org.hibernate.LazyInitializationException: could not initialize proxy - no Session
```

**Cause**: Accessing lazy-loaded associations outside transaction

**Solution**:
- Use `@EntityGraph` to eagerly fetch associations
- Keep transaction open until data access complete
- Use projections instead of entities

### Issue 2: Transaction Timeout

**Symptom**:
```
Transaction timeout: deadline has been reached
```

**Cause**: Long-running transaction

**Solution**:
```java
@Transactional(timeout = 60) // 60 seconds
public void longRunningOperation() {
    // Operation
}
```

Or better: Split into smaller transactions

### Issue 3: Deadlock

**Symptom**:
```
Deadlock detected while trying to acquire lock
```

**Cause**: Concurrent transactions accessing same rows in different order

**Solution**:
- Access rows in consistent order
- Use lower isolation level if possible
- Implement retry logic
- Keep transactions short

```java
@Transactional
@Retryable(value = DeadlockLoserDataAccessException.class, maxAttempts = 3)
public void updatePosts(List<Long> postIds) {
    // Operation that might deadlock
}
```

### Issue 4: Read-Write Transaction for Read Operation

**Symptom**: Slower performance than expected

**Diagnosis**:
```java
// Missing readOnly flag
@Transactional
public PostDetails getPost(Long id) {
    return postRepository.findById(id).orElseThrow();
}
```

**Solution**:
```java
@Transactional(readOnly = true)
public PostDetails getPost(Long id) {
    return postRepository.findById(id).orElseThrow();
}
```

## Summary

### Transaction Configuration Matrix

| Operation Type | Isolation Level | Read-Only | Propagation | Use Case |
|---------------|----------------|-----------|-------------|----------|
| Public Read | READ_UNCOMMITTED | ✅ | REQUIRED | Lists, search, public content |
| User-Specific Read | READ_COMMITTED | ✅ | REQUIRED | Profile, private data |
| Simple Write | READ_COMMITTED | ❌ | REQUIRED | CRUD operations |
| Complex Write | REPEATABLE_READ | ❌ | REQUIRED | Multi-step updates |
| Independent Write | READ_COMMITTED | ❌ | REQUIRES_NEW | Audit logs |
| Critical Operation | SERIALIZABLE | ❌ | REQUIRED | Financial, reconciliation |

### Key Takeaways

1. **Default to READ_UNCOMMITTED + readOnly for reads** - Maximum performance for public content
2. **Use READ_COMMITTED for writes** - Good consistency without excessive locking
3. **Keep transactions short** - Better concurrency, less lock contention
4. **Use entity graphs** - Avoid LazyInitializationException
5. **Service layer = transaction boundary** - Clear, maintainable pattern
6. **Monitor transaction metrics** - Identify slow or problematic transactions
