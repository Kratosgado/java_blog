# API Endpoints Documentation

## Versioning Strategy

The blog platform uses **path-based API versioning** to support multiple API versions simultaneously.

### Version Configuration

Located in: `blog-backend/src/main/java/com/kratosgado/blog/backend/config/VersionConfig.java`

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

### Base URL Structure

```
http://localhost:8080/api/{version}/{resource}
```

**Examples:**
- `/api/v1/posts` - Posts API (v1)
- `/api/v2/posts` - Posts API (v2) - New features or breaking changes
- `/api/v1/auth/login` - Authentication (v1)
- `/api/v1/admin/users` - Admin operations (v1)

## API Documentation

### Interactive API Documentation

The platform provides interactive API documentation through:

1. **Swagger UI** (OpenAPI 3.0):
   ```
   http://localhost:8080/swagger-ui.html
   ```
   - Browse all endpoints
   - Test endpoints directly from UI
   - View request/response schemas
   - Authentication testing with JWT tokens

2. **GraphQL Playground**:
   ```
   http://localhost:8080/graphiql
   ```
   - GraphQL query builder
   - Schema exploration
   - Real-time query execution

## Authentication Endpoints

**Base Path:** `/api/v1/auth`

### POST /auth/login
- **Description**: User login with JWT token generation
- **Authentication**: None (public)
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **Response**: JWT token with user details
- **Status**: 200 OK
- **Security Notes**:
  - Protected against brute-force attacks (5 failed attempts → 15-minute lockout)
  - BCrypt password verification (cost factor 12)

### POST /auth/register
- **Description**: User registration with automatic JWT token generation
- **Authentication**: None (public)
- **Request Body**:
  ```json
  {
    "email": "newuser@example.com",
    "username": "newuser",
    "password": "password123"
  }
  ```
- **Response**: JWT token, new users assigned READER role by default
- **Status**: 201 Created

### GET /auth/validate
- **Description**: Validate JWT token without checking blacklist
- **Authentication**: Bearer token (optional)
- **Request Header**:
  ```
  Authorization: Bearer <jwt-token>
  ```
- **Response**: `{ "valid": true/false, "username": "..." }`
- **Status**: 200 OK

### POST /auth/logout
- **Description**: Logout and blacklist JWT token
- **Authentication**: Bearer token (required)
- **Request Header**:
  ```
  Authorization: Bearer <jwt-token>
  ```
- **Response**: Confirmation message with token expiry
- **Status**: 200 OK
- **Note**: Token blacklist is O(1) operation, persists until natural JWT expiry (24 hours)

---

## Post Endpoints

**Base Path:** `/api/v1/posts`

### POST /posts
- **Description**: Create a new blog post
- **Authentication**: Required (AUTHOR or ADMIN role)
- **Request Body**:
  ```json
  {
    "title": "My First Post",
    "content": "Post content here",
    "excerpt": "Brief summary",
    "slug": "my-first-post",
    "categoryId": 1,
    "tagIds": [1, 2, 3],
    "status": "published"
  }
  ```
- **Response**: Created post object
- **Status**: 201 Created
- **Authorization**: `hasRole('AUTHOR') or hasRole('ADMIN')`

### GET /posts
- **Description**: Get all published posts (paginated)
- **Authentication**: None (public)
- **Query Parameters**:
  - `page`: Page number (default: 0)
  - `size`: Items per page (default: 10, max: 100)
  - `sortBy`: Sort field (default: "createdAt")
  - `sortDirection`: ASC or DESC (default: DESC)
- **Response**: Paginated list of PostView objects
- **Status**: 200 OK
- **Performance**: Cached, uses database indexes

### GET /posts/{id}
- **Description**: Get a single post by ID
- **Authentication**: None (public)
- **Response**: PostDetails object
- **Status**: 200 OK
- **Error**: 404 Not Found if post doesn't exist

### GET /posts/slug/{slug}
- **Description**: Get a single post by slug
- **Authentication**: None (public)
- **Response**: PostDetails object
- **Status**: 200 OK
- **Performance**: Cached (10-day TTL)
- **Error**: 404 Not Found if post doesn't exist

### GET /posts/search
- **Description**: Search posts by keyword
- **Authentication**: None (public)
- **Query Parameters**:
  - `query`: Search keyword
  - `page`: Page number (default: 0)
  - `size`: Items per page
  - `sortBy`: Sort field
  - `sortDirection`: ASC or DESC
- **Response**: Paginated search results
- **Status**: 200 OK
- **Performance**: Full-text search via PostgreSQL tsvector (100x faster than LIKE)

### GET /posts/user/{userId}
- **Description**: Get posts by specific user
- **Authentication**: None (public)
- **Response**: Paginated list of PostWithoutUser objects
- **Status**: 200 OK

### GET /posts/category/{categoryId}
- **Description**: Get posts in specific category
- **Authentication**: None (public)
- **Response**: Paginated list of PostWithoutCategory objects
- **Status**: 200 OK

### PUT /posts/{id}
- **Description**: Update a post
- **Authentication**: Required (AUTHOR or ADMIN role)
- **Request Body**:
  ```json
  {
    "title": "Updated Title",
    "content": "Updated content",
    "status": "published"
  }
  ```
- **Response**: Updated post object
- **Status**: 200 OK
- **Authorization**: Only post author or admin can update
- **Cache Invalidation**: Automatic cache update and list cache eviction

### PUT /posts/{id}/publish
- **Description**: Publish a draft post
- **Authentication**: Required (AUTHOR or ADMIN role)
- **Response**: Updated post object (status = "published")
- **Status**: 200 OK
- **Authorization**: Only post author or admin

### DELETE /posts/{id}
- **Description**: Delete a post
- **Authentication**: Required (AUTHOR or ADMIN role)
- **Response**: No content
- **Status**: 204 No Content
- **Authorization**: Only post author or admin can delete
- **Cache Invalidation**: Post and list caches cleared

---

## Post API v2 (Future)

**Base Path:** `/api/v2/posts`

Planned enhancements:
- Additional filtering options
- Enhanced search capabilities
- Bulk operations
- New response formats

---

## User Endpoints

**Base Path:** `/api/v1/users`

### GET /users/{id}
- **Description**: Get user profile
- **Authentication**: None (public)
- **Response**: User profile object
- **Status**: 200 OK

### GET /users
- **Description**: Get all users (paginated)
- **Authentication**: None (public)
- **Response**: Paginated list of users
- **Status**: 200 OK

---

## Category Endpoints

**Base Path:** `/api/v1/categories`

### GET /categories
- **Description**: Get all categories
- **Authentication**: None (public)
- **Response**: List of category objects
- **Status**: 200 OK
- **Performance**: Cached (2-hour TTL)

### GET /categories/{id}
- **Description**: Get category by ID
- **Authentication**: None (public)
- **Response**: Category object
- **Status**: 200 OK

### POST /categories
- **Description**: Create a new category
- **Authentication**: Required (ADMIN role)
- **Request Body**:
  ```json
  {
    "name": "Technology",
    "description": "Tech related posts"
  }
  ```
- **Response**: Created category object
- **Status**: 201 Created
- **Authorization**: `hasRole('ADMIN')`

### PUT /categories/{id}
- **Description**: Update category
- **Authentication**: Required (ADMIN role)
- **Response**: Updated category object
- **Status**: 200 OK
- **Authorization**: `hasRole('ADMIN')`

### DELETE /categories/{id}
- **Description**: Delete category
- **Authentication**: Required (ADMIN role)
- **Response**: No content
- **Status**: 204 No Content
- **Authorization**: `hasRole('ADMIN')`

---

## Tag Endpoints

**Base Path:** `/api/v1/tags`

### GET /tags
- **Description**: Get all tags
- **Authentication**: None (public)
- **Response**: List of tag objects
- **Status**: 200 OK
- **Performance**: Cached (1-hour TTL)

### GET /tags/{id}
- **Description**: Get tag by ID
- **Authentication**: None (public)
- **Response**: Tag object
- **Status**: 200 OK

### POST /tags
- **Description**: Create a new tag
- **Authentication**: Required (AUTHOR or ADMIN role)
- **Request Body**:
  ```json
  {
    "name": "java",
    "description": "Java programming language"
  }
  ```
- **Response**: Created tag object
- **Status**: 201 Created

### PUT /tags/{id}
- **Description**: Update tag
- **Authentication**: Required (AUTHOR or ADMIN role)
- **Response**: Updated tag object
- **Status**: 200 OK

### DELETE /tags/{id}
- **Description**: Delete tag
- **Authentication**: Required (AUTHOR or ADMIN role)
- **Response**: No content
- **Status**: 204 No Content

---

## Comment Endpoints

**Base Path:** `/api/v1/comments`

### GET /comments/post/{postId}
- **Description**: Get comments for a specific post
- **Authentication**: None (public)
- **Response**: List of comment objects
- **Status**: 200 OK
- **Database**: MongoDB (flexible schema)

### POST /comments
- **Description**: Create a new comment
- **Authentication**: Required (any role)
- **Request Body**:
  ```json
  {
    "postId": 1,
    "content": "Great article!",
    "parentId": null
  }
  ```
- **Response**: Created comment object
- **Status**: 201 Created
- **Database**: MongoDB

### PUT /comments/{id}
- **Description**: Update comment
- **Authentication**: Required (comment author or admin)
- **Response**: Updated comment object
- **Status**: 200 OK

### DELETE /comments/{id}
- **Description**: Delete comment
- **Authentication**: Required (comment author or admin)
- **Response**: No content
- **Status**: 204 No Content

---

## Review Endpoints

**Base Path:** `/api/v1/reviews`

### GET /reviews/post/{postId}
- **Description**: Get reviews for a specific post
- **Authentication**: None (public)
- **Response**: List of review objects
- **Status**: 200 OK
- **Database**: MongoDB (flexible schema)

### POST /reviews
- **Description**: Create/update review for a post
- **Authentication**: Required (any role)
- **Request Body**:
  ```json
  {
    "postId": 1,
    "rating": 5,
    "content": "Excellent post!"
  }
  ```
- **Response**: Created/updated review object
- **Status**: 201 Created
- **Database**: MongoDB

### DELETE /reviews/{id}
- **Description**: Delete review
- **Authentication**: Required (review author or admin)
- **Response**: No content
- **Status**: 204 No Content

---

## Role-Based Access Control (RBAC) Endpoints

The platform implements three-tier RBAC with dedicated endpoint groups:

### Admin Endpoints

**Base Path:** `/api/v1/admin` (Requires ADMIN role)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/admin/users` | GET | Get all users |
| `/admin/users/{id}/promote` | POST | Promote user role |
| `/admin/users/{id}` | DELETE | Delete user |
| `/admin/analytics` | GET | System analytics |

### Author Endpoints

**Base Path:** `/api/v1/author` (Requires AUTHOR or ADMIN role)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/author/posts` | GET | Get author's posts |
| `/author/posts/draft` | POST | Create draft post |
| `/author/posts/publish` | PUT | Publish post |
| `/author/analytics` | GET | Author analytics |

### Reader Endpoints

**Base Path:** `/api/v1/reader` (Requires READER, AUTHOR, or ADMIN role)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/reader/bookmarks` | GET | Get user bookmarks |
| `/reader/bookmarks/{postId}` | POST | Add bookmark |
| `/reader/bookmarks/{postId}` | DELETE | Remove bookmark |
| `/reader/profile` | GET | Get user profile |

---

## Performance Endpoints

**Base Path:** `/api/v1/performance`

### GET /performance/metrics
- **Description**: Get performance metrics
- **Authentication**: None (public)
- **Response**: Performance statistics
- **Status**: 200 OK

### GET /performance/cache
- **Description**: Get cache statistics
- **Authentication**: None (public)
- **Response**: Cache hit rate, size, etc.
- **Status**: 200 OK

---

## Cache Management Endpoints

**Base Path:** `/api/v1/cache`

### GET /cache/stats
- **Description**: Get cache statistics
- **Authentication**: None (public)
- **Response**: Cache stats for all cache types
- **Status**: 200 OK

### POST /cache/clear
- **Description**: Clear all caches
- **Authentication**: Required (ADMIN role)
- **Response**: Confirmation message
- **Status**: 200 OK

---

## Error Responses

All endpoints return standardized error responses:

### Error Format
```json
{
  "error": "Error type",
  "message": "Detailed error message",
  "status": 400,
  "timestamp": "2024-03-15T10:30:00Z"
}
```

### HTTP Status Codes

| Status | Meaning | Common Cause |
|--------|---------|--------------|
| 200 | OK | Successful GET, PUT |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Invalid input, validation error |
| 401 | Unauthorized | Missing/invalid JWT token |
| 403 | Forbidden | Insufficient permissions (RBAC) |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource (email, username) |
| 500 | Server Error | Unexpected server error |

### Common Error Examples

**Unauthorized (401)**:
```json
{
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header"
}
```

**Forbidden (403)**:
```json
{
  "error": "Forbidden",
  "message": "User does not have ADMIN role required for this endpoint"
}
```

**Validation Error (400)**:
```json
{
  "error": "Bad Request",
  "message": "Validation failed",
  "details": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  }
}
```

---

## JWT Token

### Token Format

All authenticated requests require a JWT token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token Claims

```json
{
  "sub": "user@example.com",
  "userId": 1,
  "roles": ["READER", "AUTHOR"],
  "iat": 1710525000,
  "exp": 1710611400
}
```

### Token Expiration

- **Expiry**: 24 hours (configurable via `JWT_EXPIRATION` env var)
- **Algorithm**: HS256
- **Secret**: Configured via `JWT_SECRET` env var

### Token Blacklist

Logout operations add tokens to a blacklist (O(1) lookup):
- Prevents token reuse after logout
- Automatically cleaned up after expiration
- In-memory storage (cleared on application restart)

---

## CORS Configuration

### Allowed Origins

Configure via `CORS_ORIGINS` environment variable:

```
CORS_ORIGINS=http://localhost:3000,http://localhost:8080,https://studio.apollographql.com
```

### Allowed Methods

- GET
- POST
- PUT
- DELETE
- OPTIONS
- PATCH

### Allowed Headers

- Authorization
- Content-Type
- Accept
- X-Requested-With
- Cache-Control

### Preflight Cache

1-hour preflight cache reduces OPTIONS request overhead.

---

## GraphQL Endpoint

**Endpoint**: `POST /graphql`

**UI**: `GET /graphiql` (development only)

GraphQL schema supports queries and mutations for all major entities:
- Posts
- Users
- Comments
- Reviews
- Tags
- Categories

---

## Rate Limiting

Currently no rate limiting is implemented. Future versions may include:
- Per-IP rate limiting
- Per-user rate limiting
- Sliding window counters

---

## API Versioning Migration Guide

### Migrating from v1 to v2

1. Change base path from `/api/v1/` to `/api/v2/`
2. Review breaking changes in v2 documentation
3. Update request/response handling for new schemas
4. Re-test authentication and RBAC

### Backward Compatibility

- v1 endpoints remain supported for 2 major versions
- Deprecation warnings provided in response headers
- Migration guide available for each endpoint

---

## Related Documentation

- [Security Configuration](SECURITY.md) - JWT, RBAC, authentication details
- [Performance Optimization](PERFORMANCE_OPTIMIZATION_REPORT.md) - Query optimization, caching
- [Database Design](DATABASE_DESIGN.md) - Schema, indexes, relationships
