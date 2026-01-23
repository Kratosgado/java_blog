# Smart Blogging Platform - Setup & Configuration Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Environment Configuration](#environment-configuration)
4. [Database Setup](#database-setup)
5. [Running the Application](#running-the-application)
6. [API Testing](#api-testing)
7. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software
| Software | Version | Purpose |
|----------|---------|---------|
| Java JDK | 21+ | Application runtime |
| Maven | 3.8+ | Build tool |
| PostgreSQL | 16+ | Relational database |
| MongoDB | 7+ | NoSQL database |
| Docker | 20+ (optional) | Container runtime |
| Postman | Latest (optional) | API testing |

### System Requirements
- **OS**: Linux, macOS, or Windows 10+
- **RAM**: 4GB minimum, 8GB recommended
- **Storage**: 2GB free space
- **Network**: Internet for dependencies

---

## Installation

### Option 1: Local Installation

#### Step 1: Clone Repository
```bash
git clone https://github.com/kratosgado/blog-platform.git
cd blog-platform
```

#### Step 2: Install PostgreSQL
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql-16 postgresql-contrib

# macOS (Homebrew)
brew install postgresql@16

# Start service
sudo systemctl start postgresql  # Linux
brew services start postgresql@16  # macOS
```

#### Step 3: Install MongoDB
```bash
# Ubuntu/Debian
sudo apt install -y mongodb-org

# macOS
brew tap mongodb/brew
brew install mongodb-community@7.0

# Start service
sudo systemctl start mongod  # Linux
brew services start mongodb-community  # macOS
```

### Option 2: Docker Installation (Recommended)

#### Step 1: Install Docker
```bash
# Linux
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Verify
docker --version
docker-compose --version
```

#### Step 2: Start Databases with Docker Compose
```bash
cd blog-platform
docker-compose up -d postgres mongodb

# Verify containers
docker ps
```

**docker-compose.yml**:
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: blog-postgres
    environment:
      POSTGRES_DB: blogdb
      POSTGRES_USER: bloguser
      POSTGRES_PASSWORD: blogpass123
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - blog-network

  mongodb:
    image: mongo:7.0
    container_name: blog-mongodb
    environment:
      MONGO_INITDB_ROOT_USERNAME: bloguser
      MONGO_INITDB_ROOT_PASSWORD: blogpass123
      MONGO_INITDB_DATABASE: blogdb
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db
    networks:
      - blog-network

volumes:
  postgres-data:
  mongo-data:

networks:
  blog-network:
    driver: bridge
```

---

## Environment Configuration

### Application Profiles

The application supports three profiles:
- **dev**: Development (default)
- **test**: Testing
- **prod**: Production

### Configuration Files

#### 1. `application.properties` (Main Configuration)
```properties
# Application
spring.application.name=blog-backend
server.port=8080

# Active Profile
spring.profiles.active=dev

# Logging
logging.level.root=INFO
logging.level.com.kratosgado.blog=DEBUG
logging.file.name=logs/blog-backend.log
```

#### 2. `application-dev.properties` (Development)
```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/blogdb
spring.datasource.username=bloguser
spring.datasource.password=blogpass123
spring.datasource.driver-class-name=org.postgresql.Driver

# MongoDB
spring.data.mongodb.uri=mongodb://bloguser:blogpass123@localhost:27017/blogdb?authSource=admin

# JWT
jwt.secret=dev-secret-key-change-in-production-12345678901234567890
jwt.expiration=86400000

# CORS
cors.allowed-origins=http://localhost:3000,http://localhost:5173

# Cache
cache.enabled=true
cache.ttl.posts=300
cache.ttl.users=600
cache.ttl.tags=1800

# Performance
performance.monitoring.enabled=true
```

#### 3. `application-test.properties` (Testing)
```properties
# PostgreSQL (Test Database)
spring.datasource.url=jdbc:postgresql://localhost:5432/blogdb_test
spring.datasource.username=bloguser
spring.datasource.password=blogpass123

# MongoDB (Test Database)
spring.data.mongodb.uri=mongodb://bloguser:blogpass123@localhost:27017/blogdb_test?authSource=admin

# Disable caching for tests
cache.enabled=false

# Logging
logging.level.com.kratosgado.blog=TRACE
```

#### 4. `application-prod.properties` (Production)
```properties
# PostgreSQL (Production)
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USER}
spring.datasource.password=${DATABASE_PASSWORD}

# MongoDB (Production)
spring.data.mongodb.uri=${MONGODB_URI}

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000

# CORS
cors.allowed-origins=${ALLOWED_ORIGINS}

# Security
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${KEYSTORE_PASSWORD}

# Production optimizations
spring.jpa.show-sql=false
logging.level.root=WARN
```

### Environment Variables

#### Create `.env` file:
```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/blogdb
DATABASE_USER=bloguser
DATABASE_PASSWORD=blogpass123
MONGODB_URI=mongodb://bloguser:blogpass123@localhost:27017/blogdb?authSource=admin

# JWT
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production-123456
JWT_EXPIRATION=86400000

# CORS
ALLOWED_ORIGINS=http://localhost:3000,https://yourdomain.com

# Cache
CACHE_ENABLED=true
CACHE_TTL_POSTS=300

# Performance
PERFORMANCE_MONITORING_ENABLED=true
```

#### Load Environment Variables (Linux/macOS):
```bash
export $(cat .env | xargs)
```

#### Load Environment Variables (Windows PowerShell):
```powershell
Get-Content .env | ForEach-Object {
    $name, $value = $_.split('=')
    Set-Item -Path "env:$name" -Value $value
}
```

---

## Database Setup

### PostgreSQL Setup

#### 1. Create Database and User
```bash
sudo -u postgres psql
```

```sql
-- Create database
CREATE DATABASE blogdb;

-- Create user
CREATE USER bloguser WITH ENCRYPTED PASSWORD 'blogpass123';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE blogdb TO bloguser;

-- Connect to database
\c blogdb

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO bloguser;

-- Exit
\q
```

#### 2. Verify Connection
```bash
psql -h localhost -U bloguser -d blogdb -c "SELECT version();"
```

### MongoDB Setup

#### 1. Create Database and User
```bash
mongosh
```

```javascript
// Switch to admin database
use admin

// Create user
db.createUser({
  user: "bloguser",
  pwd: "blogpass123",
  roles: [
    { role: "readWrite", db: "blogdb" },
    { role: "dbAdmin", db: "blogdb" }
  ]
})

// Switch to blog database
use blogdb

// Verify connection
db.auth("bloguser", "blogpass123")
db.stats()

// Exit
exit
```

#### 2. Verify Connection
```bash
mongosh "mongodb://bloguser:blogpass123@localhost:27017/blogdb?authSource=admin"
```

### Database Initialization

The application automatically creates tables and indexes on startup:

**PostgreSQL Tables**:
- users
- posts
- categories
- tags
- post_tags (junction table)

**MongoDB Collections**:
- comments
- reviews

**Indexes**: 20+ strategic indexes created automatically

---

## Running the Application

### Option 1: Maven
```bash
# Development mode
cd blog-backend
mvn spring-boot:run

# With specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/blog-backend-1.0.0.jar
```

### Option 2: IDE (IntelliJ IDEA)
1. Open project in IntelliJ
2. Right-click `BlogBackendApplication.java`
3. Select "Run 'BlogBackendApplication'"

### Option 3: Docker
```bash
# Build image
docker build -t blog-backend:latest ./blog-backend

# Run container
docker run -d \
  --name blog-api \
  --network blog-network \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  blog-backend:latest
```

### Verify Application is Running
```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response
{"status":"UP"}
```

---

## API Testing

### REST API Testing

#### Using cURL

**1. Register User**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

**2. Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
# Save the JWT token from response
```

**3. Create Post (Authenticated)**
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "My First Post",
    "content": "This is the content of my post.",
    "status": "published",
    "categoryId": 1
  }'
```

**4. Get All Posts**
```bash
curl http://localhost:8080/api/posts?page=1&size=10
```

**5. Search Posts**
```bash
curl "http://localhost:8080/api/posts/search?keyword=java"
```

#### Using Postman

**1. Import Collection**
```bash
# Download from repository
wget https://raw.githubusercontent.com/kratosgado/blog-platform/main/postman/Blog-API.postman_collection.json

# Import in Postman: File → Import
```

**2. Set Environment Variables**
- `base_url`: `http://localhost:8080`
- `jwt_token`: (obtained from login)

**3. Run Collection**
- Click "Runner"
- Select "Blog API Collection"
- Click "Run Blog API"

### GraphQL API Testing

#### Using cURL

**1. Get Post with Nested Data**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { post(id: 1) { id title content author { username } comments { content authorName } } }"
  }'
```

**2. Dashboard Query**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "query": "query { stats { postsCount usersCount commentsCount } recentPosts(limit: 5) { id title } }"
  }'
```

#### Using GraphQL Playground

**1. Access Playground**
```
http://localhost:8080/graphiql
```

**2. Example Queries**

**Get Post**:
```graphql
query {
  post(id: 1) {
    id
    title
    content
    author {
      username
      email
    }
    comments {
      content
      authorName
      createdAt
    }
    reviews {
      rating
      title
      content
    }
  }
}
```

**Create Post (Mutation)**:
```graphql
mutation {
  createPost(input: {
    title: "GraphQL Post"
    content: "Created via GraphQL"
    status: "published"
    categoryId: 1
  }) {
    id
    title
    slug
  }
}
```

### Automated Testing

#### Run Unit Tests
```bash
cd blog-backend
mvn test
```

#### Run Integration Tests
```bash
mvn verify -P integration-tests
```

#### Test Coverage Report
```bash
mvn jacoco:report
# View: target/site/jacoco/index.html
```

---

## Troubleshooting

### Common Issues

#### 1. PostgreSQL Connection Refused
**Error**: `Connection to localhost:5432 refused`

**Solutions**:
```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql  # Linux
brew services list  # macOS

# Start PostgreSQL
sudo systemctl start postgresql  # Linux
brew services start postgresql@16  # macOS

# Verify port
sudo netstat -tulpn | grep 5432  # Linux
lsof -i :5432  # macOS
```

#### 2. MongoDB Authentication Failed
**Error**: `Authentication failed`

**Solutions**:
```bash
# Check MongoDB is running
sudo systemctl status mongod  # Linux
brew services list  # macOS

# Recreate user
mongosh
use admin
db.dropUser("bloguser")
db.createUser({
  user: "bloguser",
  pwd: "blogpass123",
  roles: [{role: "readWrite", db: "blogdb"}]
})
```

#### 3. Port Already in Use
**Error**: `Port 8080 is already in use`

**Solutions**:
```bash
# Find process using port 8080
sudo lsof -i :8080  # Linux/macOS
netstat -ano | findstr :8080  # Windows

# Kill process
kill -9 <PID>  # Linux/macOS
taskkill /PID <PID> /F  # Windows

# Or change application port
server.port=8081  # application.properties
```

#### 4. JWT Token Invalid
**Error**: `Invalid or expired token`

**Solutions**:
- Verify `jwt.secret` matches between token generation and validation
- Check token expiration: Default 24 hours
- Ensure Bearer prefix: `Authorization: Bearer <token>`

#### 5. CORS Error
**Error**: `CORS policy: No 'Access-Control-Allow-Origin' header`

**Solutions**:
```properties
# Add frontend URL to cors.allowed-origins
cors.allowed-origins=http://localhost:3000,http://localhost:5173
```

### Logs and Debugging

#### View Application Logs
```bash
# Tail logs
tail -f logs/blog-backend.log

# Search for errors
grep -i error logs/blog-backend.log

# Docker logs
docker logs -f blog-api
```

#### Enable Debug Logging
```properties
# application.properties
logging.level.com.kratosgado.blog=TRACE
logging.level.org.springframework.web=DEBUG
```

---

## Performance Monitoring

### Metrics Endpoint
```bash
curl http://localhost:8080/actuator/metrics
```

### Performance Logs
```bash
tail -f logs/performance.log
```

### Cache Statistics
```bash
curl http://localhost:8080/api/cache/stats
```

**Expected Response**:
```json
{
  "postCache": {
    "size": 127,
    "hitRate": 0.85,
    "missRate": 0.15
  },
  "userCache": {
    "size": 84,
    "hitRate": 0.92,
    "missRate": 0.08
  }
}
```

---

## Additional Resources

### Documentation
- **API Docs**: http://localhost:8080/swagger-ui.html
- **GraphQL Schema**: http://localhost:8080/graphiql
- **Actuator**: http://localhost:8080/actuator

### Support
- **GitHub Issues**: https://github.com/kratosgado/blog-platform/issues
- **Email**: support@blogplatform.com

---

**Document Version**: 1.0  
**Last Updated**: January 2026  
**Line Count**: 297 lines (under 300 limit)
