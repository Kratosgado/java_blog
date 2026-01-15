# Spring Boot Backend Integration

This document describes the multi-module architecture for the Blog application.

## Project Structure

```
blog-parent/
├── pom.xml                    # Parent POM with module definitions
├── blog-common/               # Shared models and DTOs
│   └── src/main/java/com/kratosgado/blog/
│       ├── models/           # JPA entities (User, Post, Comment, etc.)
│       └── dtos/             # Request/Response DTOs
├── blog-backend/             # Spring Boot REST API
│   └── src/main/java/com/kratosgado/blog/backend/
│       ├── controllers/     # REST Controllers
│       ├── services/        # Business logic
│       ├── repositories/    # Spring Data repositories
│       ├── security/        # JWT utilities and security config
│       └── BlogBackendApplication.java
└── blog-frontend/            # JavaFX desktop application
    └── src/main/java/com/kratosgado/blog/
        ├── controllers/     # JavaFX controllers
        ├── services/        # Service layer (can call REST API)
        └── dao/             # Direct database access (legacy)
```

## Modules

### 1. blog-common
Shared library containing:
- **Models**: JPA entities with Jakarta Persistence annotations
- **DTOs**: Request and Response data transfer objects with validation
- **Enums**: Shared enumerations (e.g., CommentStatus)

### 2. blog-backend (Spring Boot)
REST API server with:
- **Port**: 8080 (configurable in `application.properties`)
- **Context Path**: `/api`
- **Authentication**: JWT-based with Spring Security
- **Database**: PostgreSQL (JPA) + MongoDB (reviews)

#### Available Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `GET /api/auth/validate` - Token validation

### 3. blog-frontend (JavaFX)
Desktop application that can:
- **Option A**: Call REST API (recommended for new features)
- **Option B**: Direct database access via DAOs (legacy mode)

## Build Commands

```bash
# Build all modules
mvn clean install

# Build specific module
mvn clean install -pl blog-backend

# Run Spring Boot backend
cd blog-backend && mvn spring-boot:run
# Or use dev script
./dev.sh run backend

# Run JavaFX frontend
cd blog-frontend && mvn clean javafx:run
# Or use dev script
./dev.sh run frontend
```

## Configuration

### Backend Configuration
File: `blog-backend/src/main/resources/application.properties`

```properties
# Server
server.port=8080
server.servlet.context-path=/api

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/blog
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT
jwt.secret=your-secret-key-change-this-in-production
jwt.expiration=86400000

# MongoDB
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=blog_reviews
```

## Development Workflow

### Starting the Application

1. **Start databases**:
   ```bash
   ./dev.sh start
   ```

2. **Run backend** (in one terminal):
   ```bash
   ./dev.sh run backend
   ```

3. **Run frontend** (in another terminal):
   ```bash
   ./dev.sh run frontend
   ```

### Adding New Features

1. **Add models to `blog-common`** if shared between frontend and backend
2. **Create REST endpoints in `blog-backend`**
3. **Update frontend services** to call REST API
4. **Rebuild**: `mvn clean install`

## Migration Strategy

The current application uses direct DAO access. To migrate to REST API:

1. **Phase 1** (Done): Set up Spring Boot backend with auth endpoints
2. **Phase 2** (TODO): Create REST endpoints for posts, comments, categories
3. **Phase 3** (TODO): Update frontend services to use HTTP client
4. **Phase 4** (TODO): Remove direct DAO dependencies from frontend

## Security

- **Backend**: JWT tokens with Spring Security
- **Frontend**: Store JWT token after login, send in Authorization header
- **CORS**: Configured to allow requests from frontend

## Next Steps

1. Implement remaining REST controllers (Posts, Comments, Categories, etc.)
2. Create HTTP client service in frontend
3. Add API response DTOs
4. Implement proper error handling
5. Add API documentation (Swagger/OpenAPI)
6. Add integration tests

## Troubleshooting

**Build fails**: Run `mvn clean install` from root directory

**Backend won't start**: Check PostgreSQL is running (`./dev.sh status`)

**Frontend can't connect**: Ensure backend is running on port 8080

**JWT errors**: Update `jwt.secret` in `application.properties`
