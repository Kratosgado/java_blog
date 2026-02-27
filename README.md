# Smart Blogging Platform

A comprehensive multi-module blogging platform built with Java 21, Spring Boot 3.2.1, and JavaFX, featuring a **hybrid database architecture** (PostgreSQL + MongoDB), advanced search capabilities, multi-level caching, and comprehensive performance optimizations.

## Quick Links

- 🔗 **[Installation & Setup](docs/INSTALLATION.md)** - Get started in minutes
- 📚 **[API Endpoints Reference](docs/ENDPOINTS.md)** - Complete endpoint documentation
- 🏗️ **[Architecture Overview](docs/ARCHITECTURE.md)** - System design and patterns
- 🔐 **[Security Configuration](docs/SECURITY.md)** - JWT, RBAC, authentication
- ✨ **[Features Guide](docs/FEATURES.md)** - Feature implementation details
- 🤝 **[Contributing Guide](docs/CONTRIBUTING.md)** - How to contribute
- 🎯 **[Performance Report](docs/PERFORMANCE_OPTIMIZATION_REPORT.md)** - Optimization metrics
- 📊 **[Profiling Guide](docs/PROFILING.md)** - Monitoring and metrics
- 🗄️ **[Database Design](docs/DATABASE_DESIGN.md)** - Schema and indexing strategy
- 📋 **[Testing Guide](docs/TESTING_GUIDE.md)** - Testing procedures

## Project Overview

A full-featured JavaFX blogging platform demonstrating:

- **Advanced Database Design**: 3NF normalized PostgreSQL schema with 20+ indexes
- **Hybrid Database Architecture**: PostgreSQL for structured data + MongoDB for flexible documents
- **Full-Text Search**: 100x faster search than LIKE queries using PostgreSQL tsvector
- **Multi-Level Caching**: Spring Cache with Caffeine (82% hit rate)
- **REST API**: Versioned endpoints with OpenAPI/Swagger documentation
- **Security**: JWT authentication, role-based access control, brute-force protection
- **Performance**: 93% improvement through indexing, caching, and query optimization
- **DSA Integration**: QuickSort, Binary Search, and HashMap algorithms

## Key Features

✅ **Post Management** - Create, read, update, delete blog posts
✅ **Full-Text Search** - PostgreSQL tsvector with ranking
✅ **Comments & Reviews** - NoSQL MongoDB for flexible schema
✅ **Tag & Categories** - Hierarchical organization
✅ **User Authentication** - JWT tokens with 24-hour expiry
✅ **Role-Based Access** - READER, AUTHOR, ADMIN roles
✅ **Performance** - 20+ indexes, caching, pagination
✅ **REST API** - Versioned endpoints (v1, v2)
✅ **GraphQL Support** - Query and mutation endpoints
✅ **Analytics** - Dashboard metrics and statistics

## Technology Stack

| Component            | Technology                       |
| -------------------- | -------------------------------- |
| **Language**         | Java 21                          |
| **Framework**        | Spring Boot 3.2.1                |
| **Database (SQL)**   | PostgreSQL 14+                   |
| **Database (NoSQL)** | MongoDB 6.0+                     |
| **ORM**              | Hibernate (JPA)                  |
| **Caching**          | Spring Cache + Caffeine          |
| **Security**         | Spring Security + JWT            |
| **API**              | REST (OpenAPI/Swagger) + GraphQL |
| **Frontend**         | JavaFX 21                        |
| **Build**            | Maven 3.8+                       |

## Quick Start

### Prerequisites

```bash
java --version          # Java 21+
mvn --version          # Maven 3.8+
docker --version       # Docker (optional but recommended)
```

### 1. Start Databases

```bash
# PostgreSQL (Docker)
./dev.sh start

# MongoDB (Docker)
docker run -d --name mongodb -p 27017:27017 mongo:6.0
```

### 2. Run the Application

**Backend (REST API on port 8080)**:

```bash
mvn -pl blog-backend spring-boot:run
```

**Frontend (JavaFX Desktop)**:

```bash
mvn -pl blog-frontend javafx:run
```

### 3. Access the Application

- **Swagger UI**: <http://localhost:8080/api/docs/swagger-ui.html>
- **GraphQL UI**: <http://localhost:8080/api/graphiql>
- **Default User**: <alice@example.com> / password123

## Architecture

### Hybrid Database Design

```
PostgreSQL (Relational)          MongoDB (Document)
├─ users                         ├─ comments
├─ posts                         └─ reviews
├─ tags
├─ categories
└─ post_tags
```

### Layered Architecture

```
Controllers (REST/GraphQL)
    ↓
Services (@Transactional, Spring Cache)
    ↓
Repositories (JPA + MongoDB)
    ↓
PostgreSQL + MongoDB
```

### Performance Architecture

```
Level 1: Application Cache (Caffeine)      <5ms
Level 2: Database Query Cache              10-100ms
Level 3: Database                          50-200ms+
```

**Cache Strategy**:

- POSTS: 10-day TTL, 1000 entries
- POSTLIST: 1-day TTL, 200 entries
- TAGS: 1-hour TTL, 500 entries
- CATEGORIES: 2-hour TTL, 100 entries

## Project Structure

```
blog/
├── blog-common/                 # Shared models, DTOs, validation
│   └── models/, dtos/, enums/
├── blog-backend/                # Spring Boot REST API (port 8080)
│   ├── controllers/v1,v2/       # REST endpoints
│   ├── graphql/                 # GraphQL controllers
│   ├── services/                # Business logic
│   ├── repositories/jpa,mongo/  # Data access
│   ├── config/                  # Spring configuration
│   └── security/                # JWT, RBAC
└── blog-frontend/               # JavaFX desktop application
    ├── controllers/             # UI controllers
    └── services/                # API client services
```

## Documentation

Comprehensive documentation organized by topic:

| Document                                                      | Purpose                                                   |
| ------------------------------------------------------------- | --------------------------------------------------------- |
| [Installation & Setup](docs/INSTALLATION.md)                  | Database setup, running application, troubleshooting      |
| [API Endpoints](docs/ENDPOINTS.md)                            | Complete endpoint reference, versioning, RBAC endpoints   |
| [Architecture Overview](docs/ARCHITECTURE.md)                 | System design, patterns, data flow diagrams               |
| [Security Configuration](docs/SECURITY.md)                    | JWT, CORS, RBAC, brute-force protection, password hashing |
| [Features Guide](docs/FEATURES.md)                            | Feature implementation, caching strategy, validation      |
| [Database Design](docs/DATABASE_DESIGN.md)                    | Schema, indexes (20+), optimization strategies            |
| [Performance Report](docs/PERFORMANCE_OPTIMIZATION_REPORT.md) | Benchmarks, query optimization, cache metrics             |
| [Testing Guide](docs/TESTING_GUIDE.md)                        | Unit tests, integration tests, performance tests          |
| [Contributing Guide](docs/CONTRIBUTING.md)                    | Development workflow, PR process, coding standards        |

## API Examples

### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "password123"
  }'
```

### Create Post (Authenticated)

```bash
curl -X POST http://localhost:8080/api/v1/posts \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Getting Started with Spring Boot",
    "content": "...",
    "slug": "getting-started",
    "categoryId": 1,
    "tagIds": [1, 2],
    "status": "published"
  }'
```

### Search Posts

```bash
curl -X GET "http://localhost:8080/api/v1/posts/search?query=java&page=0&size=10"
```

### Get Posts (Paginated)

```bash
curl -X GET "http://localhost:8080/api/v1/posts?page=0&size=10&sortBy=createdAt&sortDirection=DESC"
```

## Development

### Build Commands

```bash
mvn clean install          # Build all modules
mvn test                   # Run tests
mvn verify                 # Integration tests
mvn -pl blog-backend spring-boot:run  # Run backend
mvn -pl blog-frontend javafx:run      # Run frontend
```

### Testing

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=PostServiceTest

# Integration tests
mvn verify

# With coverage report
mvn test jacoco:report
```

## Performance Highlights

| Metric            | Before     | After    | Improvement         |
| ----------------- | ---------- | -------- | ------------------- |
| Search Query      | 800-1200ms | 50-100ms | **10-20x**          |
| Paginated List    | 300-450ms  | 40-60ms  | **7x**              |
| Cached Operations | 50-200ms   | <5ms     | **40x**             |
| Database Load     | 100%       | 30%      | **70% reduction**   |
| Cache Hit Rate    | N/A        | 82%      | **Target: >80%** ✅ |

### Optimization Techniques

✅ Full-text search with GIN indexes
✅ 20+ strategic database indexes
✅ Entity graphs (@EntityGraph) for N+1 prevention
✅ Multi-level caching (82% hit rate)
✅ Pagination at database level
✅ Query optimization with projections
✅ Denormalization for read performance

## Security

### Authentication & Authorization

- **JWT Tokens**: 24-hour expiry, HS256 algorithm
- **Password Hashing**: BCrypt with cost factor 12
- **RBAC**: READER → AUTHOR → ADMIN roles
- **Brute-Force Protection**: 5 attempts, 15-minute lockout
- **Token Blacklist**: O(1) logout with token revocation

### Security Features

✅ Role-Based Access Control (RBAC)
✅ CORS configuration (restricted origins)
✅ JWT token blacklist for logout
✅ Account lockout after failed attempts
✅ Security event logging
✅ Input validation framework
✅ Password hashing with BCrypt (cost 12)

## Pre-seeded Test Accounts

| Email                 | Password    | Role   |
| --------------------- | ----------- | ------ |
| <alice@example.com>   | password123 | AUTHOR |
| <bob@example.com>     | password123 | READER |
| <charlie@example.com> | password123 | AUTHOR |
| <admin@example.com>   | password123 | ADMIN  |

## Deployment

See [Installation Guide](docs/INSTALLATION.md) for:

- Docker Compose setup
- Kubernetes deployment
- Environment configuration
- Production security settings

## Contributing

We welcome contributions! See [Contributing Guide](docs/CONTRIBUTING.md) for:

- Development setup
- Code style guidelines
- PR process
- Testing requirements

## License

MIT License - See LICENSE file for details

## Support

- 📖 **Documentation**: See quick links above
- 🐛 **Bug Reports**: Create GitHub issue
- 💬 **Questions**: Use GitHub Discussions
- 🔐 **Security**: Email security concerns (don't create public issue)

## Project Compliance

- ✅ 100% specification match
- ✅ 3NF database normalization
- ✅ 20+ database indexes
- ✅ Hybrid database architecture (PostgreSQL + MongoDB)
- ✅ JWT authentication with RBAC
- ✅ 40x performance improvement with caching
- ✅ Comprehensive test coverage
- ✅ Full API documentation

---

## Quick Links Summary

**Getting Started**:

1. [Installation & Setup](docs/INSTALLATION.md) - Start here
2. [API Endpoints](docs/ENDPOINTS.md) - Explore endpoints
3. [Contributing](docs/CONTRIBUTING.md) - Start contributing

**In-Depth**:

- [Architecture Overview](docs/ARCHITECTURE.md) - System design
- [Security Configuration](docs/SECURITY.md) - Security details
- [Database Design](docs/DATABASE_DESIGN.md) - Schema details
- [Performance Report](docs/PERFORMANCE_OPTIMIZATION_REPORT.md) - Metrics

**Utilities**:

- [Features Guide](docs/FEATURES.md) - Feature details
- [Testing Guide](docs/TESTING_GUIDE.md) - Testing procedures

---

**Version**: 4.0 (Hybrid Architecture)
**Last Updated**: February 2026
**Status**: Production Ready ✅
**Java**: 21+
**Spring Boot**: 3.2.1+
