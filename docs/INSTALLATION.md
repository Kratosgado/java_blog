# Installation & Setup Guide

## Prerequisites

- **Java 21** or later
- **Maven 3.8+**
- **Docker** (recommended) or manual installations:
  - **PostgreSQL 14+** for structured data
  - **MongoDB 6.0+** for comments and reviews

## Option 1: Quick Start with Docker (Recommended)

### Step 1: Start PostgreSQL Container

```bash
./dev.sh start
```

This starts PostgreSQL in a Docker container named "postgis" on port 5432.

**Troubleshooting:**
- If port 5432 is already in use: `docker ps | grep postgis`
- Kill existing container: `docker stop postgis && docker rm postgis`

### Step 2: Start MongoDB Container

```bash
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  mongo:6.0
```

This starts MongoDB on port 27017.

**Troubleshooting:**
- Check status: `docker ps | grep mongodb`
- View logs: `docker logs mongodb`

### Step 3: Build and Run the Application

#### Option A: Run Backend (REST API on port 8080)

```bash
mvn -pl blog-backend spring-boot:run
```

Backend will start and automatically run database migrations.

#### Option B: Run Frontend (JavaFX Desktop Application)

```bash
mvn -pl blog-frontend javafx:run
```

Or from root directory:

```bash
mvn clean javafx:run
```

#### Option C: Build All Modules

```bash
mvn clean install
```

### Step 4: Stop Containers When Done

```bash
./dev.sh exit
docker stop mongodb
```

## Option 2: Manual Setup

### PostgreSQL Setup

#### 1. Install PostgreSQL 14+

**macOS** (using Homebrew):
```bash
brew install postgresql@14
brew services start postgresql@14
```

**Linux** (Ubuntu/Debian):
```bash
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib
sudo systemctl start postgresql
```

**Windows**:
Download from [postgresql.org](https://www.postgresql.org/download/windows/) and run installer.

#### 2. Create Database and User

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE blog_db;

-- Create user
CREATE USER blog_user WITH PASSWORD 'your_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE blog_db TO blog_user;

-- Connect to the new database and grant schema privileges
\c blog_db
GRANT ALL ON SCHEMA public TO blog_user;
```

#### 3. Initialize Database Schema

```bash
# Navigate to project root
cd /path/to/blog

# Run schema migration
psql -U blog_user -d blog_db -f blog-backend/src/main/resources/db/migration/V1__init_schema.sql

# Load sample data
psql -U blog_user -d blog_db -f blog-backend/src/main/resources/db/migration/V2__load_sample_data.sql

# Run full-text search migration (optional but recommended)
psql -U blog_user -d blog_db -f blog-backend/src/main/resources/db/migration/V3__full_text_search.sql
```

**Verify Installation:**
```bash
psql -U blog_user -d blog_db
# In psql prompt:
\dt              # List all tables
SELECT COUNT(*) FROM users;  # Should show initial user count
\q              # Exit
```

### MongoDB Setup

#### 1. Install MongoDB 6.0+

**macOS** (using Homebrew):
```bash
brew tap mongodb/brew
brew install mongodb-community@6.0
brew services start mongodb-community@6.0
```

**Linux** (Ubuntu):
```bash
curl -fsSL https://www.mongodb.org/static/pgp/server-6.0.asc | sudo apt-key add -
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu $(lsb_release -cs)/mongodb-org/6.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-6.0.list
sudo apt-get update
sudo apt-get install -y mongodb-org
sudo systemctl start mongod
```

**Windows**:
Download from [mongodb.com](https://www.mongodb.com/try/download/community) and run installer.

#### 2. Start MongoDB Service

```bash
# macOS
brew services start mongodb-community@6.0

# Linux
sudo systemctl start mongod

# Windows (if not installed as service)
mongod --dbpath "C:\Program Files\MongoDB\Server\6.0\data"
```

#### 3. Create Database and Collections

```bash
# Connect to MongoDB shell
mongosh

# Run in MongoDB shell:
use blog_nosql

# Create collections
db.createCollection("comments")
db.createCollection("reviews")

# Create indexes
db.comments.createIndex({ post_id: 1 })
db.comments.createIndex({ user_id: 1 })
db.comments.createIndex({ parent_id: 1 })
db.comments.createIndex({ created_at: -1 })

db.reviews.createIndex({ post_id: 1 })
db.reviews.createIndex({ user_id: 1 })
db.reviews.createIndex({ rating: -1 })
db.reviews.createIndex({ created_at: -1 })

# Exit
exit
```

**Verify Installation:**
```bash
mongosh blog_nosql
db.comments.find().limit(1)
db.reviews.find().limit(1)
```

### Application Configuration

#### 1. Create Environment Configuration

Create a `.env` file in the project root:

```env
# PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/blog_db
SPRING_DATASOURCE_USERNAME=blog_user
SPRING_DATASOURCE_PASSWORD=your_password

# MongoDB
MONGODB_URI=mongodb://localhost:27017
MONGODB_DB_NAME=blog_nosql

# JWT Configuration
JWT_SECRET=your-secret-key-min-256-bits-long-for-security
JWT_EXPIRATION=86400000  # 24 hours in milliseconds

# CORS Configuration
CORS_ORIGINS=http://localhost:3000,http://localhost:8080,https://studio.apollographql.com

# Server Configuration
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_KRATOSGADO=DEBUG

# Application Profile
SPRING_PROFILES_ACTIVE=dev
```

#### 2. Alternative: Application Properties File

Edit `blog-backend/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blog_db
    username: blog_user
    password: your_password

  mongodb:
    uri: mongodb://localhost:27017
    database: blog_nosql

jwt:
  secret: your-secret-key-min-256-bits-long-for-security
  expiration: 86400000

server:
  port: 8080
  servlet:
    context-path: /api

cors:
  origins: http://localhost:3000,http://localhost:8080

logging:
  level:
    root: INFO
    com.kratosgado: DEBUG
```

#### 3. Build and Run Application

```bash
# Clean build
mvn clean install

# Run backend
mvn -pl blog-backend spring-boot:run

# Or run frontend
mvn -pl blog-frontend javafx:run
```

## Build Commands Reference

```bash
# Clean and compile all modules
mvn clean compile

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PostServiceTest

# Run integration tests
mvn verify

# Package application
mvn clean package

# Run backend with Spring DevTools (hot reload)
mvn -pl blog-backend spring-boot:run -Dspring-boot.run.arguments="--spring.devtools.restart.enabled=true"

# Run frontend
mvn -pl blog-frontend javafx:run

# Check for dependency updates
mvn versions:display-dependency-updates

# Generate javadoc
mvn javadoc:javadoc

# View generated javadoc
open blog-backend/target/site/apidocs/index.html
```

## Accessing the Application

### Backend (REST API)

Once started on port 8080:

1. **Swagger UI** (API Documentation & Testing):
   ```
   http://localhost:8080/api/swagger-ui.html
   ```

2. **OpenAPI Schema**:
   ```
   http://localhost:8080/api/v3/api-docs
   ```

3. **Health Check**:
   ```
   http://localhost:8080/api/actuator/health
   ```

### Frontend (JavaFX)

Launches automatically when running:
```bash
mvn -pl blog-frontend javafx:run
```

## Pre-seeded Test Accounts

The database is seeded with the following test users:

| Username | Email | Password | Role |
|----------|-------|----------|------|
| alice | alice@example.com | password123 | AUTHOR |
| bob | bob@example.com | password123 | READER |
| charlie | charlie@example.com | password123 | AUTHOR |
| admin | admin@example.com | password123 | ADMIN |

**Testing Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "alice@example.com",
    "username": "alice",
    "roles": ["AUTHOR"]
  }
}
```

## Environment Setup for Different Profiles

### Development Profile (default)

```bash
mvn -pl blog-backend spring-boot:run -Dspring.profiles.active=dev
```

**Features:**
- Debug logging enabled
- H2 in-memory database (optional)
- Swagger UI enabled
- DevTools hot reload

### Production Profile

```bash
mvn -pl blog-backend spring-boot:run -Dspring.profiles.active=prod
```

**Features:**
- Info logging level
- Database connection pooling optimized
- CORS restricted to production domains
- Swagger UI disabled

### Test Profile

```bash
mvn test -Dspring.profiles.active=test
```

**Features:**
- H2 in-memory database
- Test data fixtures
- Mock external services

## Troubleshooting

### PostgreSQL Connection Issues

**Error:** `Connection refused`
```bash
# Check if PostgreSQL is running
psql -U postgres -c "SELECT version();"

# If not running, start it:
brew services start postgresql@14  # macOS
sudo systemctl start postgresql    # Linux
```

**Error:** `FATAL: role "blog_user" does not exist`
```bash
# Create the user
psql -U postgres -c "CREATE USER blog_user WITH PASSWORD 'your_password';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE blog_db TO blog_user;"
```

### MongoDB Connection Issues

**Error:** `Connection refused`
```bash
# Check if MongoDB is running
brew services list  # macOS
systemctl status mongod  # Linux

# If not running, start it:
brew services start mongodb-community@6.0  # macOS
sudo systemctl start mongod  # Linux
```

### Port Already in Use

**Error:** `Address already in use: bind`

Find and kill the process using the port:
```bash
# Find process on port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use different port
mvn -pl blog-backend spring-boot:run -Dserver.port=8081
```

### Java Version Issues

**Error:** `Unsupported class version`
```bash
# Check Java version
java -version

# Should be 21 or later. Install if needed:
# Using SDKMAN
sdk install java 21.0.0-oracle
sdk use java 21.0.0-oracle

# Or download from oracle.com
```

### Maven Build Failures

**Error:** `BUILD FAILURE: dependencies not found`
```bash
# Clear Maven cache and rebuild
mvn clean install -U

# Or
rm -rf ~/.m2/repository/com/kratosgado
mvn clean install
```

## Docker Compose (Alternative Setup)

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    environment:
      POSTGRES_DB: blog_db
      POSTGRES_USER: blog_user
      POSTGRES_PASSWORD: your_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  mongodb:
    image: mongo:6.0
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

  blog-backend:
    build:
      context: .
      dockerfile: blog-backend/Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/blog_db
      SPRING_DATASOURCE_USERNAME: blog_user
      SPRING_DATASOURCE_PASSWORD: your_password
      MONGODB_URI: mongodb://mongodb:27017
    depends_on:
      - postgres
      - mongodb

volumes:
  postgres_data:
  mongodb_data:
```

Start all services:
```bash
docker-compose up -d
```

## Next Steps

1. **Read the API Documentation**: Visit http://localhost:8080/api/swagger-ui.html
2. **Test Authentication**: Use provided test accounts to login
3. **Explore Database Schema**: See [Database Design](DATABASE_DESIGN.md)
4. **Review Security Configuration**: See [Security Guide](SECURITY.md)
5. **Understand Architecture**: See [Architecture Overview](ARCHITECTURE.md)

## Related Documentation

- [API Endpoints](ENDPOINTS.md) - Complete endpoint reference
- [Security Configuration](SECURITY.md) - JWT, RBAC, authentication
- [Architecture Overview](ARCHITECTURE.md) - System design and patterns
- [Database Design](DATABASE_DESIGN.md) - Schema and optimization
