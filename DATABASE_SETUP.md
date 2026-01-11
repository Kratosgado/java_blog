# Database Setup Guide

This guide provides step-by-step instructions for setting up the PostgreSQL database for the Smart Blogging Platform.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Database Creation](#database-creation)
- [Schema Setup](#schema-setup)
- [Seeding Sample Data](#seeding-sample-data)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)
- [Database Management](#database-management)

---

## Prerequisites

Before setting up the database, ensure you have:

1. **PostgreSQL 14 or higher** installed
2. **psql** command-line tool (comes with PostgreSQL)
3. **Java 21** (for Java-based seeder)
4. **Maven** (for building the project)

### Installing PostgreSQL

#### On Ubuntu/Debian

```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

#### On macOS (using Homebrew)

```bash
brew install postgresql@14
brew services start postgresql@14
```

#### On Windows

Download and install from [PostgreSQL official website](https://www.postgresql.org/download/windows/)

---

## Installation

### 1. Start PostgreSQL Service

```bash
# On Linux
sudo systemctl start postgresql

# On macOS
brew services start postgresql@14

# On Windows
# PostgreSQL service starts automatically after installation
```

### 2. Access PostgreSQL

```bash
# Switch to postgres user (Linux)
sudo -u postgres psql

# Direct access (macOS/Windows)
psql -U postgres
```

---

## Database Creation

### Method 1: Using psql Command Line

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE blog;

-- Create user (if needed)
CREATE USER blogadmin WITH PASSWORD 'your_password_here';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE blog TO blogadmin;

-- Exit
\q
```

### Method 2: Using Shell Script

```bash
# Create database and user
createdb -U postgres blog
createuser -U postgres blogadmin
psql -U postgres -c "ALTER USER blogadmin WITH PASSWORD 'your_password_here';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE blog TO blogadmin;"
```

---

## Schema Setup

There are two ways to set up the database schema:

### Method 1: Using SQL Script (Recommended)

The `schema.sql` file contains the complete database schema with all tables, indexes, views, and triggers.

```bash
# From project root directory
psql -U postgres -d blog -f schema.sql
```

**What this creates:**

- ✅ 6 tables: users, posts, comments, tags, post_tags, reviews
- ✅ 20+ performance indexes
- ✅ 2 views: post_statistics, popular_posts
- ✅ 3 triggers for automatic timestamp updates
- ✅ All foreign key constraints and relationships
- ✅ Check constraints for data validation

### Method 2: Automatic Schema Creation (Alternative)

The application automatically creates tables when you first run it:

```bash
# Set database credentials in .env file
DB_URL=jdbc:postgresql://localhost:5432/blog
DB_USER=postgres
DB_PASSWORD=your_password

# Run the application
mvn javafx:run
```

The DAO classes will automatically create tables on first initialization.

---

## Seeding Sample Data

After creating the schema, you can populate the database with sample data.

### Method 1: Using SQL Seed Script (Recommended)

The `seed.sql` file contains comprehensive sample data:

```bash
# Load seed data
psql -U postgres -d blog -f seed.sql
```

**Sample data includes:**

- 8 users (password: "password123" for all)
- 14 blog posts (12 published, 2 drafts)
- 15 tags (Java, JavaFX, Database, etc.)
- 30 comments
- 40+ post-tag relationships
- 25 reviews

### Method 3: Manual Data Entry

Use the application's UI to create users, posts, and other content manually.

---

## Verification

After setup, verify everything is working correctly:

### 1. Check Tables

```sql
-- Connect to database
psql -U postgres -d blog

-- List all tables
\dt

-- Expected output:
-- users, posts, comments, tags, post_tags, reviews
```

### 2. Check Indexes

```sql
-- View all indexes
SELECT tablename, indexname
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
```

### 3. Check Data Counts

```sql
-- Count records in each table
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Posts', COUNT(*) FROM posts
UNION ALL
SELECT 'Comments', COUNT(*) FROM comments
UNION ALL
SELECT 'Tags', COUNT(*) FROM tags
UNION ALL
SELECT 'Post_Tags', COUNT(*) FROM post_tags
UNION ALL
SELECT 'Reviews', COUNT(*) FROM reviews;
```

**Expected counts (if using seed.sql):**

```
Users:     8
Posts:     14
Comments:  30
Tags:      15
Post_Tags: 40+
Reviews:   25
```

### 4. Test Queries

```sql
-- Get published posts with author info
SELECT p.id, p.title, u.username, p.views
FROM posts p
JOIN users u ON p.user_id = u.id
WHERE p.status = 'published'
ORDER BY p.views DESC
LIMIT 5;

-- Get post statistics
SELECT * FROM post_statistics LIMIT 5;

-- Get popular posts
SELECT * FROM popular_posts;
```

---

## Troubleshooting

### Issue: "psql: command not found"

**Solution:** Add PostgreSQL to your PATH:

```bash
# On Linux
export PATH=/usr/lib/postgresql/14/bin:$PATH

# On macOS
export PATH=/usr/local/opt/postgresql@14/bin:$PATH
```

### Issue: "FATAL: role does not exist"

**Solution:** Create the user:

```bash
sudo -u postgres createuser your_username
```

### Issue: "Connection refused"

**Solution:** Start PostgreSQL service:

```bash
# Linux
sudo systemctl start postgresql

# macOS
brew services start postgresql@14
```

### Issue: "Permission denied for database"

**Solution:** Grant privileges:

```sql
psql -U postgres
GRANT ALL PRIVILEGES ON DATABASE blog TO your_username;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_username;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO your_username;
```

### Issue: "Table already exists" error

**Solution:** Drop and recreate:

```sql
-- CAUTION: This deletes all data!
DROP DATABASE blog;
CREATE DATABASE blog;

-- Then run schema.sql again
psql -U postgres -d blog -f schema.sql
```

---

## Database Management

### Backup Database

```bash
# Full database backup
pg_dump -U postgres blog > blog_backup_$(date +%Y%m%d).sql

# Schema only (no data)
pg_dump -U postgres --schema-only blog > blog_schema_$(date +%Y%m%d).sql

# Data only (no schema)
pg_dump -U postgres --data-only blog > blog_data_$(date +%Y%m%d).sql
```

### Restore Database

```bash
# Restore from backup
psql -U postgres blog < blog_backup_20260107.sql
```

### Reset Database

```bash
# Drop all data (keep schema)
psql -U postgres -d blog -f reset.sql

# Or recreate everything from scratch
dropdb -U postgres blog
createdb -U postgres blog
psql -U postgres -d blog -f schema.sql
psql -U postgres -d blog -f seed.sql
```

---

## Database Configuration

### Update .env File

Create or update the `.env` file in the project root:

```properties
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/blog
DB_USER=postgres
DB_PASSWORD=your_password_here

# Optional: Connection Pool Settings
DB_MAX_POOL_SIZE=20
DB_MIN_IDLE=5
DB_CONNECTION_TIMEOUT=30000
```

### Environment Variables (Alternative)

```bash
export DB_URL="jdbc:postgresql://localhost:5432/blog"
export DB_USER="postgres"
export DB_PASSWORD="your_password"
```

---

## Performance Optimization

### Analyze Tables

```sql
-- Update table statistics for query optimization
ANALYZE users;
ANALYZE posts;
ANALYZE comments;
ANALYZE tags;
ANALYZE reviews;

-- Or analyze all tables
ANALYZE;
```

### Vacuum Database

```sql
-- Reclaim storage and update statistics
VACUUM ANALYZE;

-- Full vacuum (requires exclusive lock)
VACUUM FULL;
```

### Monitor Slow Queries

```sql
-- Enable query logging
ALTER DATABASE blog SET log_min_duration_statement = 1000; -- Log queries > 1s

-- View slow queries
SELECT
  query,
  calls,
  total_time,
  mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

---

## Security Best Practices

1. **Never use default passwords in production**
2. **Create separate users with limited privileges:**

```sql
-- Read-only user for reporting
CREATE USER readonly WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE blog TO readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly;

-- Application user with specific permissions
CREATE USER appuser WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE blog TO appuser;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO appuser;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO appuser;
```

1. **Enable SSL connections in production:**

```properties
DB_URL=jdbc:postgresql://localhost:5432/blog?ssl=true&sslmode=require
```

1. **Regular backups:**

```bash
# Set up automated daily backups
0 2 * * * /usr/bin/pg_dump -U postgres blog > /backups/blog_$(date +\%Y\%m\%d).sql
```

---

## Quick Start Commands

### Complete Setup (Fresh Start)

```bash
# 1. Create database
createdb -U postgres blog

# 2. Create schema
psql -U postgres -d blog -f schema.sql

# 3. Load sample data
psql -U postgres -d blog -f seed.sql

# 4. Verify
psql -U postgres -d blog -c "SELECT COUNT(*) FROM users;"

# 5. Run application
mvn javafx:run
```

### Reset Everything

```bash
# CAUTION: This deletes all data!
dropdb -U postgres blog && \
createdb -U postgres blog && \
psql -U postgres -d blog -f schema.sql && \
psql -U postgres -d blog -f seed.sql
```

---

## Additional Resources

- [PostgreSQL Official Documentation](https://www.postgresql.org/docs/)
- [PostgreSQL Performance Tips](https://wiki.postgresql.org/wiki/Performance_Optimization)
- [Database Design Best Practices](https://www.postgresqltutorial.com/postgresql-best-practices/)

---

## Support

If you encounter any issues:

1. Check the [Troubleshooting](#troubleshooting) section
2. Review application logs in `logs/` directory
3. Verify database connection in `.env` file
4. Ensure PostgreSQL service is running
5. Check PostgreSQL logs: `/var/log/postgresql/` (Linux) or Console app (macOS)

---

**Last Updated:** January 7, 2026  
**Database Version:** PostgreSQL 14+  
**Schema Version:** 1.0
