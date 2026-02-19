# Architecture Overview

## System Architecture

The Smart Blogging Platform is built on a comprehensive **layered architecture** with **hybrid database design** supporting both relational (PostgreSQL) and document (MongoDB) data models.

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      Frontend Layer                              │
│  ┌─────────────────┐              ┌──────────────────┐          │
│  │  JavaFX Desktop │  OR  REST    │  Web Browser     │          │
│  │   Application   │<────────────>│  (React/Angular) │          │
│  └─────────────────┘              └──────────────────┘          │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP/REST
┌──────────────────────────▼──────────────────────────────────────┐
│                    API Gateway Layer                             │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Spring Security (JWT Authentication & Authorization)     │  │
│  │  CORS Configuration & Request Validation                  │  │
│  └───────────────────────────────────────────────────────────┘  │
└────────┬─────────────────────────────────────────────────────────┘
         │
┌────────▼──────────────────────────────────────────────────────────┐
│                   REST Controllers Layer                          │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐   │
│  │ /v1/ Endpoints   │  │ /v2/ Endpoints   │  │ GraphQL API   │   │
│  │ (v1 Controllers) │  │ (v2 Controllers) │  │ (GraphQL Ctrl)│   │
│  └──────────────────┘  └──────────────────┘  └───────────────┘   │
└────────┬──────────────────────────────────────────────────────────┘
         │
┌────────▼──────────────────────────────────────────────────────────┐
│                 Business Logic Layer (Services)                   │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ @Transactional Services with Spring Cache Integration       │  │
│  │ ┌───────────────┐  ┌────────────────┐  ┌──────────────────┐  │  │
│  │ │ PostService   │  │ UserService    │  │ CommentService   │  │  │
│  │ │ TagService    │  │ CategoryService│  │ ReviewService    │  │  │
│  │ │ AuthService   │  │ etc.           │  │ SearchService    │  │  │
│  │ └───────────────┘  └────────────────┘  └──────────────────┘  │  │
│  └─────────────────────────────────────────────────────────────┘  │
│           ↓         ↓           ↓        ↓           ↓            │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Caching Layer (Spring Cache with Caffeine)                  │  │
│  │  - PostCache (10 days TTL, LRU eviction)                     │  │
│  │  - UserCache (1 hour TTL)                                    │  │
│  │  - TagCache (1 hour TTL)                                     │  │
│  │  - CategoryCache (2 hours TTL)                               │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────┬──────────────────────────────────────────────────────────┘
         │
┌────────▼──────────────────────────────────────────────────────────┐
│              Data Access Layer (Repositories)                     │
│  ┌──────────────────────────────────────┐                         │
│  │  JPA Repositories (PostgreSQL)       │                         │
│  │  ┌──────────────────────────────────┐                         │
│  │  │ PostRepository                   │ @Query JPQL/Native      │
│  │  │ UserRepository                   │ @EntityGraph (@1-N)     │
│  │  │ TagRepository                    │ Projection Interfaces   │
│  │  │ CategoryRepository               │ Pagination Support      │
│  │  │ etc.                             │                         │
│  │  └──────────────────────────────────┘                         │
│  └──────────────────────────────────────┘                         │
│  ┌──────────────────────────────────────┐                         │
│  │  MongoDB Repositories (Comments)     │                         │
│  │  ┌──────────────────────────────────┐                         │
│  │  │ CommentMongoDAO                  │ MongoCollection API     │
│  │  │ ReviewMongoDAO                   │ Manual Pagination       │
│  │  │ Custom Queries                   │ Flexible Schema         │
│  │  └──────────────────────────────────┘                         │
│  └──────────────────────────────────────┘                         │
└────────┬──────────────────────────────────────────────────────────┘
         │
┌────────▼──────────────────────────────────────────────────────────┐
│                   Database Layer                                  │
│  ┌──────────────────────────┐  ┌──────────────────────────────┐  │
│  │   PostgreSQL 14+         │  │  MongoDB 6.0+                │  │
│  │  (Relational Data)       │  │  (Document Data)             │  │
│  │                          │  │                              │  │
│  │ Tables:                  │  │ Collections:                 │  │
│  │ - users                  │  │ - comments                   │  │
│  │ - posts                  │  │ - reviews                    │  │
│  │ - tags                   │  │                              │  │
│  │ - categories             │  │ Indexes:                     │  │
│  │ - post_tags (junction)   │  │ - post_id, user_id           │  │
│  │ - comments (archived)    │  │ - created_at, rating         │  │
│  │ - reviews (archived)     │  │                              │  │
│  │                          │  │ Features:                    │  │
│  │ Features:                │  │ - Flexible schema            │  │
│  │ - 20+ indexes            │  │ - Threaded comments          │  │
│  │ - Full-text search       │  │ - Nested documents           │  │
│  │ - Views & Triggers       │  │ - Aggregation pipeline       │  │
│  │ - Constraints            │  │                              │  │
│  └──────────────────────────┘  └──────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘
```

## Module Structure

### Multi-Module Maven Project

```
blog/
├── blog-parent (root POM)
│   ├── blog-common
│   │   └── Shared domain models, DTOs, enums, validation
│   ├── blog-backend
│   │   └── Spring Boot REST API server (port 8080)
│   └── blog-frontend
│       └── JavaFX desktop client
```

### Module Dependencies

```
blog-frontend
    │
    └──→ blog-common
             ↑
             │
blog-backend
    │
    └──→ blog-common
```

**Key Principle**: Common has **zero dependencies** on other modules.

## Layered Architecture

### 1. Presentation Layer (Controllers)

**Location**: `blog-backend/src/main/java/.../controllers/`

**Responsibility**: Handle HTTP requests, request validation, response formatting

**Components**:

- REST Controllers (v1, v2 packages)
- GraphQL Controllers
- RBAC-specific controllers (AdminController, AuthorController, ReaderController)
- OpenAPI annotations for Swagger documentation

**Pattern**:

```java
@RestController
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Post management")
public class PostController {
    @PostMapping
    @SecuredCreateEndpoint
    public Post createPost(@Valid @RequestBody CreatePostRequest request) {
        return postService.createPost(request);
    }
}
```

### 2. Business Logic Layer (Services)

**Location**: `blog-backend/src/main/java/.../services/`

**Responsibility**: Implement business logic, transaction management, caching

**Key Annotations**:

- `@Service` - Service stereotype
- `@Transactional` - Declarative transaction management
- `@Cacheable` - Read from cache
- `@CacheEvict` - Invalidate cache entries
- `@Caching` - Combine multiple cache operations

**Pattern**:

```java
@Service
@Transactional(readOnly = true)
public class PostService {
    @Cacheable(value = "POSTS", key = "#slug")
    public PostDetails findBySlug(String slug) {
        return postRepository.findBySlug(slug).orElseThrow();
    }

    @Transactional
    @CacheEvict(value = "POSTLIST", allEntries = true)
    public PostDetails createPost(CreatePostRequest request) {
        Post post = new Post();
        // ... creation logic
        return postRepository.save(post);
    }
}
```

### 3. Data Access Layer (Repositories)

**PostgreSQL Repositories**:

- Location: `blog-backend/src/main/java/.../repositories/jpa/`
- Extend: `JpaRepository<Entity, ID>`
- Features:
  - Custom `@Query` methods with JPQL/Native SQL
  - `@EntityGraph` for eager loading
  - Projection interfaces for DTOs
  - Pagination & sorting support

**MongoDB Repositories**:

- Location: `blog-backend/src/main/java/.../repositories/mongo/`
- Manual implementation using MongoClient
- Features:
  - Document-based CRUD
  - Custom pagination
  - Flexible schema support

**DAO Pattern Example**:

```java
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.user u
        JOIN FETCH p.category c
        WHERE p.slug = :slug
    """)
    Optional<Post> findBySlug(@Param("slug") String slug);

    @EntityGraph(value = "Post.full")
    Optional<PostDetails> findById(Long id);
}
```

### 4. Database Layer

#### PostgreSQL (Relational Data)

**Core Tables**:

- `users` - User accounts and authentication
- `posts` - Blog articles
- `tags` - Post tags
- `categories` - Post categories
- `post_tags` - Many-to-many junction table

**Optimization Features**:

- 20+ B-Tree indexes on foreign keys, search fields, timestamps
- GIN indexes for full-text search
- Views for pre-computed aggregations
- Triggers for automatic updates

#### MongoDB (Document Data)

**Collections**:

- `comments` - Threaded comments with flexible metadata
- `reviews` - Post reviews with nested ratings

**Optimization Features**:

- Compound indexes on frequently queried fields
- Flexible schema for evolving data structures

## Hybrid Database Architecture

### When to Use Each Database

| Aspect           | PostgreSQL               | MongoDB                       |
| ---------------- | ------------------------ | ----------------------------- |
| **Data Type**    | Structured, relational   | Semi-structured, flexible     |
| **Schema**       | Fixed schema, migrations | Dynamic schema, no migrations |
| **Transactions** | ACID transactions        | Single-document atomicity     |
| **Queries**      | SQL with joins           | JSON queries, aggregation     |
| **Use Cases**    | Users, Posts, Tags       | Comments, Reviews             |

### Data Flow

```
User Request
    │
    ├─→ PostService
    │       │
    │       ├─→ PostRepository (PostgreSQL)
    │       │       ├─→ JPA Query
    │       │       └─→ Database
    │       │
    │       └─→ CommentMongoDAO (MongoDB)
    │               ├─→ MongoDB Collection
    │               └─→ Database
    │
    └─→ Response DTO
```

## Caching Strategy

### Caffeine Cache Configuration

```yaml
Cache Name    | TTL    | Max Size | Purpose
POSTS         | 10 days| 1000     | Individual posts
POSTLIST      | 1 day  | 200      | Paginated lists
TAGS          | 1 hour | 500      | Tag data
CATEGORIES    | 2 hours| 100      | Category data
USERS         | 1 hour | 100      | User profiles
```

### Cache Lifecycle

```
1. READ Operation
   ├─→ Check cache (O(1) lookup)
   ├─→ Cache HIT: Return cached value (~5ms)
   └─→ Cache MISS: Query DB, store in cache, return

2. WRITE Operation
   ├─→ Update single item cache (@CachePut)
   ├─→ Invalidate list cache (@CacheEvict with allEntries=true)
   └─→ Return updated value

3. DELETE Operation
   ├─→ Remove specific entry
   ├─→ Invalidate related caches
   └─→ Evict from memory
```

### Cache Performance

- **Hit Rate**: 82% (target >80%)
- **Hit Time**: <5ms
- **Miss Time**: 50-200ms
- **Overall Improvement**: 40x faster for cached operations

## Security Architecture

### Authentication & Authorization Flow

```
HTTP Request
    │
    ├─→ JwtAuthenticationFilter
    │   ├─→ Extract JWT from Authorization header
    │   ├─→ Validate signature and expiry
    │   ├─→ Check token blacklist (O(1) lookup)
    │   ├─→ Load user from database
    │   └─→ Set Security Context
    │
    ├─→ Controller @RoleGuardAspect
    │   ├─→ Check user roles
    │   ├─→ Verify permissions
    │   └─→ Grant/deny access
    │
    └─→ Service Method
        └─→ Execute business logic
```

### RBAC Model

```
READER (Base role for all authenticated users)
  ├── Can browse posts
  ├── Can read comments and reviews
  ├── Can create comments
  └── Can bookmark posts

AUTHOR (Extends READER)
  ├── All READER permissions
  ├── Can create/edit own posts
  ├── Can publish posts
  ├── Can manage own comments
  └── Can create reviews

ADMIN (Full access)
  ├── All AUTHOR permissions
  ├── Can edit/delete any post
  ├── Can manage users
  ├── Can view analytics
  └── Can access system administration endpoints
```

## API Versioning Strategy

### Path-Based Versioning

```java
@Configuration
public class VersionConfig implements WebMvcConfigurer {
  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix("/v1",
        HandlerTypePredicate.forBasePackage("com.kratosgado.blog.backend.controllers.v1"));

    configurer.addPathPrefix("/v2",
        HandlerTypePredicate.forBasePackage("com.kratosgado.blog.backend.controllers.v2"));
  }
}
```

### URL Structure

```
/api/v1/posts       → controllers.v1.PostController
/api/v2/posts       → controllers.v2.PostControllerV2
/api/v1/auth/login  → controllers.v1.AuthController
/api/v1/admin/users → controllers.v1.AdminController
```

## Performance Optimization Architecture

### Query Optimization

```
Strategy                | Implementation        | Performance
Full-Text Search        | PostgreSQL tsvector  | 100x vs LIKE
Entity Graph (N+1)      | @EntityGraph         | 15x faster
Pagination              | LIMIT/OFFSET         | Constant memory
Denormalization         | Cached fields        | Join elimination
Indexing                | 20+ strategic indexes| O(log n) lookup
```

### Cache Hierarchy

```
Level 1: Application Cache (Caffeine)
  ├─→ Hit Rate: 82%
  ├─→ Response Time: <5ms
  └─→ Size: ~245MB

Level 2: Database Query Cache
  ├─→ Index scans
  ├─→ Response Time: 10-100ms
  └─→ Automatically managed by PostgreSQL

Level 3: Database
  └─→ Response Time: 50-200ms+
```

## Data Access Patterns

### Repository Pattern

```java
// JPA Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findBySlug(String slug);
    Page<Post> findByStatus(String status, Pageable pageable);
}

// Service Layer
@Service
public class PostService {
    public Post getBySlug(String slug) {
        return postRepository.findBySlug(slug)
            .orElseThrow(() -> new BlogException("Post not found"));
    }
}
```

### Projection Pattern

```java
// Projection interface (Spring Data)
public interface PostDetails {
    Long getId();
    String getTitle();
    String getContent();
    UserView getAuthor();

    interface UserView {
        String getName();
        String getAvatarUrl();
    }
}
```

### DTO Pattern

```java
// Request DTO (Record)
public record CreatePostRequest(
    @NotBlank String title,
    @NotBlank String content,
    Long categoryId,
    List<Long> tagIds
) {}

// Response DTO
public record PostResponse(
    Long id,
    String title,
    String content,
    UserDto author,
    LocalDateTime createdAt
) {}
```

## Error Handling Architecture

### Exception Hierarchy

```
Throwable
  └── Exception
       └── BlogException (custom)
            ├── NotFoundException
            ├── BadRequestException
            ├── UnauthorizedException
            └── ForbiddenException
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BlogException.class)
    public ResponseEntity<?> handleBlogException(BlogException e) {
        return ResponseEntity
            .status(e.getHttpStatus())
            .body(new ErrorResponse(e.getMessage(), e.getHttpStatus()));
    }
}
```

## Integration Points

### External Services

1. **Email Service** (Future)
   - User registration confirmation
   - Password reset notifications

2. **File Storage** (Future)
   - Featured images
   - User avatars

3. **OAuth2**
   - Google login

## Technology Stack

| Layer         | Technology                        |
| ------------- | --------------------------------- |
| **Language**  | Java 21                           |
| **Framework** | Spring Boot 3.2.1                 |
| **ORM**       | Hibernate (JPA)                   |
| **Caching**   | Spring Cache + Caffeine           |
| **Database**  | PostgreSQL 14+, MongoDB 6.0+      |
| **Security**  | Spring Security, JWT              |
| **API**       | REST (SpringDoc OpenAPI), GraphQL |
| **Frontend**  | JavaFX 21                         |
| **Build**     | Maven 3.8+                        |
| **Testing**   | JUnit 5, Mockito                  |

## Related Documentation

- [API Endpoints](ENDPOINTS.md) - Complete endpoint reference
- [Security Configuration](SECURITY.md) - Detailed security setup
- [Database Design](DATABASE_DESIGN.md) - Schema and optimization
- [Performance Optimization Report](PERFORMANCE_OPTIMIZATION_REPORT.md) - Benchmarks and metrics
- [Installation Guide](INSTALLATION.md) - Setup instructions
