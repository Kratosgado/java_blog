# API Endpoints Documentation

This document summarizes the REST and GraphQL endpoints for the blogging platform, and the standardized response wrapper used across the API.

## Versioning Strategy

- Path-based API versioning. Controllers are namespaced per version.
- Base URL: `http://localhost:8080/api/{version}/{resource}`
- Examples: `/api/v1/posts`, `/api/v1/auth/login`, `/api/v2/posts` (future)

- OAuth endpoint: `http://localhost:8080/api/oauth2/authorization/google`

Location: `blog-backend/src/main/java/com/kratosgado/blog/backend/config/VersionConfig.java`

## Interactive API Documentation

- Swagger UI (OpenAPI): `http://localhost:8080/api/docs/swagger-ui.html`
- GraphQL Playground: `http://localhost:8080/api/graphiql`

## Standard Response Wrapper

All REST endpoints return a standardized JSON wrapper using `ResponseDto<T>` (see `blog-common/src/main/java/com/kratosgado/blog/dtos/response/ResponseDto.java`). Fields:

- `status` (int): HTTP status code
- `message` (string): Human-readable message
- `data` (object): Response payload for successful requests (omitted when null)
- `errors` (object): Error details for failed requests (omitted when null)

Examples:

Success (200):

```json
{
  "status": 200,
  "message": "OK",
  "data": {
    /* resource */
  }
}
```

Created (201):

```json
{
  "status": 201,
  "message": "Created",
  "data": { "id": 123, "slug": "my-first-post" }
}
```

Validation / Error:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": { "email": "Invalid email format" }
}
```

## Authentication (`/api/v1/auth`)

POST /auth/login

- Public. Body: `{ email, password }`.
- Response: `ResponseDto.data` contains token + user details. `status` = 200.

POST /auth/register

- Public. Body: `{ email, username, password }`.
- Response: `status` = 201, `data` contains token + user.

GET /auth/validate

- Optional bearer token. Response: `status` = 200 and validation result in `data`.

POST /auth/logout

- Protected. Blacklists token. Response: `status` = 200, `message` confirms logout.

Security notes

- BCrypt password hashing. Login protection (temporary lockouts after multiple failures).

## Posts (`/api/v1/posts`)

POST /posts

- Protected (AUTHOR|ADMIN). Body: create post fields.
- Response: `status` = 201, `data` contains created post projection.

GET /posts

- Public. Pagination via `page`, `size`, `sortBy`, `sortDirection`.
- Response: `status` = 200, `data` is paginated list of `PostView` projection. Cached (list cache).

GET /posts/{id}

- Public. Response: `data` = `PostDetails`. `404` sets `status` in wrapper.

GET /posts/slug/{slug}

- Public. Response: `data` = `PostDetails`. Cached (POSTS cache, 10-day TTL).

GET /posts/search

- Public. Full-text search (Postgres tsvector). Returns paginated `data`.

PUT /posts/{id}

- Protected (author or admin). Response: `status` = 200, `data` updated post. Cache updated/evicted accordingly.

PUT /posts/{id}/publish

- Protected. Changes post to published. Response: `200` and updated `data`.

DELETE /posts/{id}

- Protected (author or admin). Response: `status` = 204 (no data). Caches cleared.

## Users (`/api/v1/users`)

GET /users/{id}

- Public. Response: `status` = 200, `data` = user profile projection.

GET /users

- Public. Paginated list in `data`.

## Categories (`/api/v1/categories`)

GET /categories

- Public. `status` = 200, `data` list. Cached (2-hour TTL).

GET /categories/{id}

- Public. `data` = category.

POST /categories

- ADMIN only. `status` = 201, `data` created category.

PUT /categories/{id}

- ADMIN only. `status` = 200, `data` updated category.

DELETE /categories/{id}

- ADMIN only. `status` = 204.

## Tags (`/api/v1/tags`)

GET /tags

- Public. `status` = 200, `data` list. Cached (1-hour TTL).

GET /tags/{id}

- Public. `data` = tag.

POST /tags

- AUTHOR|ADMIN. `status` = 201, `data` created tag.

PUT /tags/{id}

- AUTHOR|ADMIN. `status` = 200, `data` updated tag.

DELETE /tags/{id}

- AUTHOR|ADMIN. `status` = 204.

## Comments (`/api/v1/comments`) — MongoDB

GET /comments/post/{postId}

- Public. `status` = 200, `data` = list of comments (from MongoDB).

POST /comments

- Protected (any authenticated). `status` = 201, `data` created comment.

PUT /comments/{id}

- Protected (author or admin). `status` = 200, `data` updated comment.

DELETE /comments/{id}

- Protected (author or admin). `status` = 204.

## Reviews (`/api/v1/reviews`) — MongoDB

GET /reviews/post/{postId}

- Public. `status` = 200, `data` list of reviews.

POST /reviews

- Protected. Create/update a review. `status` = 201.

DELETE /reviews/{id}

- Protected (author or admin). `status` = 204.

## Role-Based Access Control (RBAC)

- ADMIN endpoints: `/api/v1/admin/*` (manage users, analytics, etc.).
- AUTHOR endpoints: `/api/v1/author/*` (manage author posts and analytics).
- READER endpoints: `/api/v1/reader/*` (bookmarks, profile).

Example admin endpoints:
`GET /api/v1/admin/users`, `POST /api/v1/admin/users/{id}/promote`, `DELETE /api/v1/admin/users/{id}`

## Performance & Cache Endpoints (`/api/v1/performance`, `/api/v1/cache`)

GET /performance/metrics

- Public. `status` = 200, `data` = performance stats.

GET /performance/cache

- Public. `status` = 200, `data` = cache statistics.

GET /cache/stats

- Public. `status` = 200, `data` = cache stats.

POST /cache/clear

- ADMIN only. Clears caches. `status` = 200, `message` confirms action.

## Error Handling

All endpoints return the `ResponseDto` wrapper. Errors set `status` and `message`. When applicable, include `errors` with field-level details.

Common examples:

Unauthorized (401):

```json
{ "status": 401, "message": "Missing or invalid Authorization header" }
```

Forbidden (403):

```json
{
  "status": 403,
  "message": "User does not have ADMIN role required for this endpoint"
}
```

Validation Error (400):

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  }
}
```

HTTP status code mapping is the same as the `status` value in the wrapper; controllers set both the HTTP status and the `ResponseDto.status` consistently.

## JWT

- Token must be sent in `Authorization: Bearer <token>` header.
- Claims include `sub`, `userId`, `roles`, `iat`, `exp`.
- Expiration default: 24 hours (configurable via `JWT_EXPIRATION`).
- Algorithm: HS256; secret via `JWT_SECRET` env var. Logout adds tokens to an in-memory blacklist (O(1) lookup).

## CORS

- Configure allowed origins with `CORS_ORIGINS` env var (e.g. `http://localhost:3000,http://localhost:8080`).
- Allowed methods: GET, POST, PUT, DELETE, OPTIONS, PATCH.

## GraphQL

- Endpoint: `POST /api/graphql` (GraphiQL UI at `/graphiql` in dev).

## Rate Limiting

- Not implemented currently. Future work may include per-IP and per-user sliding-window limits.

## Migration Guide & Compatibility

- When migrating v1 → v2: change base path, review breaking changes, update client handling for new schemas, re-test RBAC and auth.
- v1 remains supported for 2 major versions; deprecation warnings may be provided via headers.

## Related Docs

- Security: `SECURITY.md`
- Performance: `PERFORMANCE_OPTIMIZATION_REPORT.md`
- Database design: `DATABASE_DESIGN.md`

--

For more details about DTOs and projections, see `blog-common` and `blog-backend` source packages.
