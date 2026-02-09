# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A multi-module Spring Boot + JavaFX blogging platform with hybrid database architecture (PostgreSQL + MongoDB). Features REST API with versioning, GraphQL support, JWT authentication, multi-level caching, and comprehensive performance optimizations.

**Key Technologies**: Java 21, Spring Boot 3.2.1, JavaFX 21, PostgreSQL 14+, MongoDB 6.0+, Maven 3.8+

## Common Development Commands

### Build & Run

```bash
# Build all modules
mvn clean install

# Run backend (Spring Boot REST API on port 8080)
mvn -pl blog-backend spring-boot:run

# Run frontend (JavaFX desktop application)
mvn -pl blog-frontend javafx:run

# Run specific module
mvn clean javafx:run  # From root (runs frontend by default)

# Package for deployment
mvn clean package
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PostServiceTest

# Run tests in specific module
mvn -pl blog-backend test

# Integration tests
mvn verify
```

### Database Management

```bash
# Start PostgreSQL container (dev.sh script)
./dev.sh start

# Stop PostgreSQL container
./dev.sh exit

# Start MongoDB container
docker run -d --name mongodb -p 27017:27017 mongo:6.0

# Stop MongoDB
docker stop mongodb

# Database initialization script
./setup-databases.sh
```

### Development Tools

```bash
# Run with Spring DevTools hot reload
mvn -pl blog-backend spring-boot:run

# API Documentation (Swagger UI)
# Start backend, then visit: http://localhost:8080/swagger-ui.html

# GraphQL Playground (GraphiQL)
# Visit: http://localhost:8080/graphiql
```

## Architecture Overview

### Multi-Module Structure

**blog-parent** (root POM)
- **blog-common**: Shared domain models, DTOs, enums, validation
- **blog-backend**: Spring Boot REST API server (port 8080)
- **blog-frontend**: JavaFX desktop client

### Module Dependencies
- Frontend depends on Common
- Backend depends on Common
- Common has no dependencies on other modules

### Hybrid Database Architecture

**PostgreSQL** (Relational - Structured Data):
- Users, Posts, Tags, Categories
- JPA with Hibernate ORM
- 20+ indexes for query optimization
- Full-text search with GIN indexes

**MongoDB** (Document - Flexible Schema):
- Comments (threaded discussions)
- Reviews (ratings with flexible metadata)
- MongoDB sync driver (not Spring Data MongoDB in backend)

### API Versioning

Path-based versioning with controller package structure:
- `/api/v1/**` → `controllers.v1` package
- `/api/v2/**` → `controllers.v2` package (future)

Configured in `VersionConfig.java` using `WebMvcConfigurer` requestMapping.

### Caching Strategy

**Caffeine Cache** (configured in `CacheConfig.java`):
- `POSTS`: 10 days TTL, 1000 max entries
- `POSTLIST`: 1 day TTL, 200 max entries
- `TAGS`: 1 hour TTL, 500 max entries
- `TAGLIST`: 1 hour TTL, 500 max entries
- `CATEGORIES`: 2 hours TTL, 100 max entries
- `CATEGORYLIST`: 2 hours TTL, 100 max entries

**Spring Cache Annotations**:
- `@Cacheable`: Read from cache or execute
- `@CacheEvict`: Invalidate entries
- `@CachePut`: Update cache
- `@Caching`: Combine multiple cache operations

### Security

**JWT-based Authentication**:
- Stateless session management
- Token expiration: 24 hours (configurable via `JWT_EXPIRATION`)
- Secret key: configurable via `JWT_SECRET` env var

**Endpoint Security** (in `SecurityConfig.java`):
- Public: `/api/v1/auth/**`, `/swagger-ui/**`, `/graphiql`, GET requests on posts/tags/categories
- Protected (requires JWT): POST/PUT/DELETE operations
- Admin-only: `/api/v1/admin/**`

**Password Security**: BCrypt hashing (cost factor 10)

## Key Architectural Patterns

### Repository Layer

**PostgreSQL (JPA Repositories)** in `repositories.jpa`:
- Extend `JpaRepository<Entity, ID>`
- Custom queries with `@Query` annotation
- Use `@EntityGraph` to prevent N+1 problems
- Projection interfaces for DTO mapping (e.g., `PostView`, `PostDetails`)
- Example: `PostRepository.findBySlug()`, `findByStatus()`

**MongoDB Repositories** in `repositories.mongo`:
- Manual implementation using `MongoClient`
- CRUD operations with `MongoCollection<Document>`
- Custom pagination logic
- Example: `CommentRepository.findByPostId()`

### Service Layer

**Transactional Services**:
- `@Service` + `@Transactional` annotations
- Default: `@Transactional(readOnly = true)`
- Write operations: `@Transactional` (read-write)
- Cache annotations on service methods

**Pattern**:
```java
@Service
@Transactional(readOnly = true)
public class PostService {
    @Cacheable(value = BlogConstants.POSTS, key = "#slug")
    public PostResponse.PostDetails findBySlug(String slug) { ... }

    @Transactional
    @CacheEvict(value = BlogConstants.POSTLIST, allEntries = true)
    public PostResponse.PostDetails createPost(CreatePostRequest request) { ... }
}
```

### DTO Pattern

**Request DTOs** (`dtos.request`):
- Java records for immutability
- Jakarta validation annotations
- Example: `CreatePostRequest`, `UpdatePostRequest`, `PageRequest`

**Response Projections** (`dtos.response`):
- Interface-based projections for Spring Data JPA
- Nested interfaces for different views
- Example: `PostResponse.PostDetails`, `PostResponse.PostView`

**Conversion**: Repositories return projections directly via Spring Data query methods

### Error Handling

**Custom Exception** (`BlogException`):
- Static factory methods: `.notFound()`, `.badRequest()`, `.forbidden()`, `.unauthorized()`
- Maps to HTTP status codes
- Example: `throw BlogException.notFound("Post not found with slug: " + slug);`

**Global Exception Handler**: Returns standardized error responses

### Pagination

**Request**: `PageRequest` record with `page`, `size`, `sortBy`, `sortDirection`
- Default: page=0, size=10
- Max size: 100

**Response**: `PageResponse<T>` with `content`, `totalElements`, `totalPages`, `currentPage`, `pageSize`

**Implementation**: Spring Data `Pageable` → database LIMIT/OFFSET

## Important Implementation Details

### Entity Relationships

**Post Entity**:
- `@NamedEntityGraph` with attributeNodes: `user`, `category`, `tags`
- Use `@EntityGraph(value = "Post.full")` in repository methods to eagerly fetch relations
- Denormalized fields: `authorName`, `authorAvatarUrl` (reduces JOINs)

**Many-to-Many**:
- Post ↔ Tag: Junction table managed by JPA
- Post ↔ Category: Junction table managed by JPA

**Cascade Rules**:
- User deletion → CASCADE to Posts, Comments, Reviews
- Post deletion → CASCADE to Comments, Reviews
- Category deletion → SET NULL on Posts

### Performance Optimizations

**Indexing**:
- Foreign keys: `idx_posts_user_id`, `idx_posts_category_id`
- Search fields: `idx_posts_slug`, `idx_posts_title`, `idx_posts_status`
- Timestamps: `idx_posts_created_at` (for sorting)
- Full-text: GIN index on `search_vector` (PostgreSQL tsvector)

**N+1 Query Prevention**:
- Use `@EntityGraph` in repository methods
- Fetch joins in JPQL: `JOIN FETCH p.user u`
- Example: `PostRepository.findBySlug()` uses `Post.full` entity graph

**Query Optimization**:
- Projection interfaces return only needed fields
- Pagination at database level (LIMIT/OFFSET)
- Native queries for complex aggregations

### Configuration Management

**Environment Variables** (`.env` file or system env):
```bash
# PostgreSQL
DB_URL=jdbc:postgresql://localhost:5432/blog_db
DB_USER=blog_user
DB_PASS=blog_password

# MongoDB
MONGO_URI=mongodb://localhost:27017
MONGO_DB_NAME=blog_nosql

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000  # 24 hours in ms

# CORS
CORS_ORIGINS=http://localhost:3000,http://localhost:8081

# Server
PORT=8080
```

**Spring Profiles**:
- `application.yml`: Base configuration
- `application-dev.yml`: Development (default)
- `application-prod.yml`: Production
- `application-test.yml`: Testing

Activate profile: `spring.profiles.active=dev` (in application.yml or via `-Dspring.profiles.active=prod`)

### GraphQL Support

**Schema Location**: `src/main/resources/graphql/schema.graphqls`

**Controllers**: `graphql` package with `@QueryMapping`, `@MutationMapping`, `@Argument` annotations

**Endpoint**: `POST /graphql` with GraphiQL UI at `/graphiql` (dev only)

### Logging & Monitoring

**AOP Aspects** (in `aspects` package):
- `LoggingAspect`: Logs method entry/exit with execution time
- `PerformanceAspect`: Tracks service method performance
- `PerformanceMonitoringAspect`: Custom performance metrics

**Logging Configuration**: SLF4J with Logback (Spring Boot default)

### Testing Approach

**Unit Tests**:
- Mockito for service mocking
- `@SpringBootTest` for integration tests
- `@DataJpaTest` for repository tests

**Test Data**: DataFaker for realistic test data generation

**Authentication Tests**: Spring Security Test with `@WithMockUser`

## Common Development Patterns

### Adding a New Entity

1. Create model in `blog-common/src/main/java/.../models/`
2. Add JPA annotations: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
3. Create repository in `blog-backend/.../repositories/jpa/`
4. Create DTOs in `blog-common/.../dtos/`
5. Implement service in `blog-backend/.../services/`
6. Add controller in `blog-backend/.../controllers/v1/`
7. Consider caching strategy and add to `CacheConfig` if needed

### Adding a New API Endpoint

1. Create/update request DTO with validation
2. Create/update response projection interface
3. Add service method with `@Transactional` and cache annotations
4. Add controller method with OpenAPI annotations
5. Test with Swagger UI at `/swagger-ui.html`

### Cache Invalidation Pattern

When updating data:
```java
@Transactional
@Caching(
    put = @CachePut(value = BlogConstants.POSTS, key = "#id"),
    evict = @CacheEvict(value = BlogConstants.POSTLIST, allEntries = true)
)
public PostResponse.PostDetails updatePost(Long id, UpdatePostRequest request) {
    // Update single item cache + invalidate list cache
}
```

### MongoDB Operations

Located in `repositories.mongo`:
```java
MongoCollection<Document> collection =
    MongoDBConfig.getDatabase().getCollection("comments");

// Insert
collection.insertOne(document);

// Find
collection.find(Filters.eq("post_id", postId))
    .limit(pageSize)
    .skip(page * pageSize);

// Update
collection.updateOne(filter, Updates.set("field", value));
```

## Project Structure Reference

```
blog/
├── blog-common/              # Shared models and DTOs
│   └── src/main/java/com/kratosgado/blog/
│       ├── models/           # JPA entities (Post, User, Tag, Category, Comment, Review)
│       ├── dtos/
│       │   ├── request/      # Request records with validation
│       │   └── response/     # Response projection interfaces
│       └── enums/            # PostStatus, CommentStatus, UserRole
│
├── blog-backend/             # Spring Boot REST API
│   └── src/main/java/com/kratosgado/blog/backend/
│       ├── config/           # Configuration classes
│       │   ├── CacheConfig.java
│       │   ├── SecurityConfig.java
│       │   ├── VersionConfig.java
│       │   ├── DataSourceConfig.java
│       │   └── MongoDBConfig.java
│       ├── controllers/
│       │   └── v1/           # REST endpoints (/api/v1/**)
│       ├── graphql/          # GraphQL controllers
│       ├── services/         # Business logic with @Transactional
│       ├── repositories/
│       │   ├── jpa/          # PostgreSQL repositories
│       │   └── mongo/        # MongoDB repositories
│       ├── aspects/          # AOP logging and monitoring
│       └── exceptions/       # Custom exception handling
│   └── src/main/resources/
│       ├── application*.yml  # Profile-based configuration
│       └── graphql/          # GraphQL schema
│
└── blog-frontend/            # JavaFX desktop client
    └── src/main/java/com/kratosgado/blog/
        ├── controllers/      # JavaFX UI controllers
        ├── services/         # API client services (OkHttp)
        └── utils/            # Navigator, AuthContext, validators
```

## Key Files to Check

When working on specific features:

**Authentication**: `SecurityConfig.java`, `JwtUtil.java`, `AuthService.java`
**Caching**: `CacheConfig.java`, service layer annotations
**API Versioning**: `VersionConfig.java`, controller package structure
**Database Config**: `application-*.yml`, `DataSourceConfig.java`, `MongoDBConfig.java`
**Entity Relationships**: Model classes with `@ManyToOne`, `@ManyToMany`, `@EntityGraph`
**Query Optimization**: Repository methods with `@EntityGraph`, `@Query`
**Error Handling**: `BlogException.java`, global exception handler
**DTOs**: `dtos.request` and `dtos.response` packages

## Important Constraints

**Data Validation**:
- Username: 3-50 characters
- Password: BCrypt hashed, validated on input
- Email: Valid email format
- Post status: 'draft', 'published', or 'archived'
- Review rating: 1-5
- Comment content: 1-5000 characters

**Performance Limits**:
- Max page size: 100
- Cache TTLs: Posts (10 days), Tags (1 hour), Categories (2 hours)
- JWT expiration: 24 hours

**Database Constraints**:
- Unique: username, email, tag names, category names
- NOT NULL: Required fields on all entities
- Foreign key constraints with appropriate CASCADE/SET NULL rules
