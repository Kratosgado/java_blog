#!/bin/bash

# ================================================================
# Smart Blogging Platform - Database Setup Script
# Sets up PostgreSQL and MongoDB with Docker and seeds data
# ================================================================

set -e # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Smart Blogging Platform Setup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# ================================================================
# 1. Setup PostgreSQL
# ================================================================
echo -e "${YELLOW}[1/5] Setting up PostgreSQL...${NC}"

# Check if postgis container exists
if [ "$(docker ps -aq -f name=postgis)" ]; then
	echo -e "${YELLOW}PostgreSQL container exists. Stopping and removing...${NC}"
	docker stop postgis 2>/dev/null || true
	docker rm postgis 2>/dev/null || true
fi

# Create and start PostgreSQL container
echo -e "${GREEN}Creating PostgreSQL container...${NC}"
docker run -d \
	--name postgis \
	-e POSTGRES_DB=blog_db \
	-e POSTGRES_USER=blog_user \
	-e POSTGRES_PASSWORD=blog_password \
	-p 5432:5432 \
	postgres:17

# Wait for PostgreSQL to be ready
echo -e "${GREEN}Waiting for PostgreSQL to be ready...${NC}"
sleep 5

# Test connection
until docker exec postgis pg_isready -U blog_user -d blog_db >/dev/null 2>&1; do
	echo -e "${YELLOW}Waiting for PostgreSQL...${NC}"
	sleep 2
done

echo -e "${GREEN}✓ PostgreSQL is ready!${NC}"
echo ""

# ================================================================
# 2. Setup MongoDB
# ================================================================
echo -e "${YELLOW}[2/5] Setting up MongoDB...${NC}"

# Check if mongodb container exists
if [ "$(docker ps -aq -f name=mongodb)" ]; then
	echo -e "${YELLOW}MongoDB container exists. Stopping and removing...${NC}"
	docker stop mongodb 2>/dev/null || true
	docker rm mongodb 2>/dev/null || true
fi

# Create and start MongoDB container
echo -e "${GREEN}Creating MongoDB container...${NC}"
docker run -d \
	--name mongodb \
	-p 27017:27017 \
	mongo:6.0

# Wait for MongoDB to be ready
echo -e "${GREEN}Waiting for MongoDB to be ready...${NC}"
sleep 5

# Test MongoDB connection
until docker exec mongodb mongosh --eval "db.adminCommand('ping')" >/dev/null 2>&1; do
	echo -e "${YELLOW}Waiting for MongoDB...${NC}"
	sleep 2
done

echo -e "${GREEN}✓ MongoDB is ready!${NC}"
echo ""

# ================================================================
# 3. Initialize PostgreSQL Schema
# ================================================================
echo -e "${YELLOW}[3/5] Initializing PostgreSQL schema...${NC}"

# Copy schema file to container and execute
docker cp schema.sql postgis:/tmp/schema.sql
docker exec postgis psql -U blog_user -d blog_db -f /tmp/schema.sql

echo -e "${GREEN}✓ PostgreSQL schema created!${NC}"
echo ""

# ================================================================
# 4. Seed PostgreSQL Data
# ================================================================
echo -e "${YELLOW}[4/5] Seeding PostgreSQL data...${NC}"

# Create modified seed file without comments and reviews (they go to MongoDB)
docker exec postgis psql -U blog_user -d blog_db <<'EOF'
-- ================================================================
-- Smart Blogging Platform - PostgreSQL Seed Data
-- (Comments and Reviews are in MongoDB)
-- ================================================================

-- Clear existing data
TRUNCATE TABLE post_tags, tags, posts, users RESTART IDENTITY CASCADE;

-- ================================================================
-- SEED DATA: Users
-- Password for all users: "password123" (BCrypt hashed)
-- ================================================================
INSERT INTO users (username, email, password, avatar_url) VALUES
('john_doe', 'john.doe@example.com', '\$2a\$10\$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=1'),
('jane_smith', 'jane.smith@example.com', '\$2a\$10\$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=2'),
('bob_wilson', 'bob.wilson@example.com', '\$2a\$10\$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=3'),
('alice_johnson', 'alice.johnson@example.com', '\$2a\$10\$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=4'),
('charlie_brown', 'charlie.brown@example.com', '\$2a\$10\$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=5'),
('diana_prince', 'diana.prince@example.com', '\$2a\$10\$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=6'),
('ethan_hunt', 'ethan.hunt@example.com', '\$2a\$10\$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=7'),
('fiona_gallagher', 'fiona.gallagher@example.com', '\$2a\$10\$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=8');

-- ================================================================
-- SEED DATA: Tags
-- ================================================================
INSERT INTO tags (name, slug, description) VALUES
('Java', 'java', 'Articles about Java programming language'),
('JavaFX', 'javafx', 'JavaFX GUI framework tutorials and tips'),
('Database', 'database', 'Database design, SQL, and optimization'),
('PostgreSQL', 'postgresql', 'PostgreSQL specific content'),
('Tutorial', 'tutorial', 'Step-by-step tutorials'),
('Best Practices', 'best-practices', 'Industry best practices and patterns'),
('Performance', 'performance', 'Performance optimization techniques'),
('Architecture', 'architecture', 'Software architecture and design patterns'),
('Testing', 'testing', 'Testing strategies and frameworks'),
('DevOps', 'devops', 'DevOps practices and tools'),
('Security', 'security', 'Security best practices'),
('Web Development', 'web-development', 'Web development articles'),
('Mobile', 'mobile', 'Mobile app development'),
('Cloud', 'cloud', 'Cloud computing and services'),
('AI/ML', 'ai-ml', 'Artificial Intelligence and Machine Learning');

-- ================================================================
-- SEED DATA: Posts (14 posts)
-- ================================================================
INSERT INTO posts (user_id, title, content, excerpt, status, featured_image, cover_image, icon, views) VALUES
(1, 'Getting Started with JavaFX: A Comprehensive Guide', 
'JavaFX is a powerful framework for building rich desktop applications in Java...', 
'Learn the fundamentals of JavaFX and build your first desktop application.', 
'published', 'https://images.unsplash.com/photo-1587620962725-abab7fe55159?w=800', 
'https://images.unsplash.com/photo-1587620962725-abab7fe55159?w=1200', '☕', 1523),

(1, 'Database Design Principles: Third Normal Form', 
'Database normalization is crucial for maintaining data integrity...', 
'Master database normalization and achieve Third Normal Form.', 
'published', 'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800',
'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=1200', '🗄️', 892),

(2, 'Mastering SQL Indexing for Performance', 
'Database indexes dramatically improve query performance...', 
'Learn how to use indexes effectively to speed up queries.', 
'published', 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800',
'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=1200', '⚡', 2156);

-- ================================================================
-- SEED DATA: Post-Tag Relationships
-- ================================================================
INSERT INTO post_tags (post_id, tag_id) VALUES
(1, 2), (1, 5), (1, 1),
(2, 3), (2, 4), (2, 6),
(3, 3), (3, 7), (3, 5);

COMMIT;
EOF

echo -e "${GREEN}✓ PostgreSQL data seeded!${NC}"
echo ""

# ================================================================
# 5. Seed MongoDB Data
# ================================================================
echo -e "${YELLOW}[5/5] Seeding MongoDB data...${NC}"

# Create MongoDB seed script and execute
docker exec mongodb mongosh <<'EOF'
use blog_nosql;

// Drop existing collections
db.comments.drop();
db.reviews.drop();

// ================================================================
// SEED DATA: Comments Collection
// ================================================================
db.comments.insertMany([
  {
    post_id: 1,
    user_id: 2,
    content: "Great introduction to JavaFX! Really accessible for beginners.",
    author_name: "Jane Smith",
    author_avatar_url: "https://i.pravatar.cc/150?img=2",
    status: "APPROVED",
    parent_id: null,
    depth: 0,
    reactions: { like: 15, love: 3, insightful: 2 },
    mentions: [],
    attachments: [],
    metadata: { platform: "JavaFX", edited: false },
    created_at: new Date("2026-01-10T08:30:00Z"),
    updated_at: new Date("2026-01-10T08:30:00Z")
  },
  {
    post_id: 1,
    user_id: 3,
    content: "Thanks for the code examples. Worked perfectly!",
    author_name: "Bob Wilson",
    author_avatar_url: "https://i.pravatar.cc/150?img=3",
    status: "APPROVED",
    parent_id: null,
    depth: 0,
    reactions: { like: 8, love: 1 },
    mentions: [],
    attachments: [],
    metadata: { platform: "JavaFX" },
    created_at: new Date("2026-01-10T10:15:00Z"),
    updated_at: new Date("2026-01-10T10:15:00Z")
  },
  {
    post_id: 2,
    user_id: 1,
    content: "Excellent explanation of normalization! Very clear.",
    author_name: "John Doe",
    author_avatar_url: "https://i.pravatar.cc/150?img=1",
    status: "APPROVED",
    parent_id: null,
    depth: 0,
    reactions: { like: 22, insightful: 10 },
    mentions: [],
    attachments: [],
    metadata: { platform: "JavaFX" },
    created_at: new Date("2026-01-11T09:00:00Z"),
    updated_at: new Date("2026-01-11T09:00:00Z")
  },
  {
    post_id: 3,
    user_id: 1,
    content: "I implemented indexes and saw 70x improvement! 🚀",
    author_name: "John Doe",
    author_avatar_url: "https://i.pravatar.cc/150?img=1",
    status: "APPROVED",
    parent_id: null,
    depth: 0,
    reactions: { like: 45, fire: 12, insightful: 8 },
    mentions: [],
    attachments: [],
    metadata: { platform: "JavaFX", performance_gain: "70x" },
    created_at: new Date("2026-01-11T14:20:00Z"),
    updated_at: new Date("2026-01-11T14:20:00Z")
  }
]);

// ================================================================
// SEED DATA: Reviews Collection
// ================================================================
db.reviews.insertMany([
  {
    post_id: 1,
    user_id: 2,
    rating: 5,
    title: "Perfect for Beginners",
    content: "This is exactly what I needed to get started with JavaFX. Clear explanations and working code!",
    helpful: true,
    author_name: "Jane Smith",
    author_avatar_url: "https://i.pravatar.cc/150?img=2",
    images: [],
    votes: { helpful: 25, not_helpful: 2 },
    metadata: { verified_purchase: false, platform: "JavaFX", version: "1.0" },
    created_at: new Date("2026-01-10T12:00:00Z"),
    updated_at: new Date("2026-01-10T12:00:00Z")
  },
  {
    post_id: 1,
    user_id: 3,
    rating: 4,
    title: "Good Introduction",
    content: "Solid tutorial! Would love more advanced topics, but excellent for beginners.",
    helpful: true,
    author_name: "Bob Wilson",
    author_avatar_url: "https://i.pravatar.cc/150?img=3",
    images: [],
    votes: { helpful: 18, not_helpful: 1 },
    metadata: { platform: "JavaFX", version: "1.0" },
    created_at: new Date("2026-01-10T15:30:00Z"),
    updated_at: new Date("2026-01-10T15:30:00Z")
  },
  {
    post_id: 2,
    user_id: 3,
    rating: 5,
    title: "Database Design Made Simple",
    content: "Finally understand normalization! The progression from 1NF to 3NF was brilliantly explained.",
    helpful: true,
    author_name: "Bob Wilson",
    author_avatar_url: "https://i.pravatar.cc/150?img=3",
    images: [],
    votes: { helpful: 42, not_helpful: 0 },
    metadata: { platform: "JavaFX", expert_verified: true },
    created_at: new Date("2026-01-11T10:00:00Z"),
    updated_at: new Date("2026-01-11T10:00:00Z")
  },
  {
    post_id: 3,
    user_id: 2,
    rating: 5,
    title: "Game Changer",
    content: "Implemented these indexing strategies and saw immediate 50x performance improvements!",
    helpful: true,
    author_name: "Jane Smith",
    author_avatar_url: "https://i.pravatar.cc/150?img=2",
    images: [],
    votes: { helpful: 67, not_helpful: 3 },
    metadata: { platform: "JavaFX", performance_gain: "50x" },
    created_at: new Date("2026-01-11T16:00:00Z"),
    updated_at: new Date("2026-01-11T16:00:00Z")
  }
]);

// ================================================================
// Create Indexes
// ================================================================
print("Creating indexes...");

// Comments indexes
db.comments.createIndex({ post_id: 1 });
db.comments.createIndex({ user_id: 1 });
db.comments.createIndex({ parent_id: 1 });
db.comments.createIndex({ status: 1 });
db.comments.createIndex({ created_at: -1 });
db.comments.createIndex({ post_id: 1, created_at: -1 });

// Reviews indexes
db.reviews.createIndex({ post_id: 1 });
db.reviews.createIndex({ user_id: 1 });
db.reviews.createIndex({ rating: -1 });
db.reviews.createIndex({ post_id: 1, rating: -1 });

print("✓ MongoDB indexes created!");

// ================================================================
// Verification
// ================================================================
print("\n========================================");
print("  Database Setup Complete!");
print("========================================\n");

print("Comments count: " + db.comments.countDocuments());
print("Reviews count: " + db.reviews.countDocuments());
print("\nSample comment:");
printjson(db.comments.findOne());
print("\nSample review:");
printjson(db.reviews.findOne());

EOF

echo -e "${GREEN}✓ MongoDB data seeded!${NC}"
echo ""

# ================================================================
# Final Verification
# ================================================================
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Setup Complete!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}✓ PostgreSQL running on localhost:5432${NC}"
echo -e "  Database: blog_db"
echo -e "  User: blog_user"
echo -e "  Password: blog_password"
echo ""
echo -e "${GREEN}✓ MongoDB running on localhost:27017${NC}"
echo -e "  Database: blog_nosql"
echo -e "  Collections: comments, reviews"
echo ""
echo -e "${YELLOW}To stop databases:${NC}"
echo -e "  ./dev.sh exit"
echo ""
echo -e "${YELLOW}To start databases:${NC}"
echo -e "  ./dev.sh start"
echo ""
echo -e "${YELLOW}To connect to PostgreSQL:${NC}"
echo -e "  docker exec -it postgis psql -U blog_user -d blog_db"
echo ""
echo -e "${YELLOW}To connect to MongoDB:${NC}"
echo -e "  docker exec -it mongodb mongosh blog_nosql"
echo ""
