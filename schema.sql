-- ================================================================
-- Smart Blogging Platform - Database Schema
-- Database: PostgreSQL 17.2+
-- Architecture: Hybrid (PostgreSQL + MongoDB)
-- Normalization: Third Normal Form (3NF)
-- ================================================================

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS post_categories CASCADE;
DROP TABLE IF EXISTS post_tags CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS tags CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ================================================================
-- TABLE: users
-- Description: Stores user account information with profile data
-- ================================================================
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL, -- BCrypt hashed password
  email VARCHAR(100) NOT NULL UNIQUE,
  avatar_url VARCHAR(500),
  bio TEXT,
  website VARCHAR(255),
  location VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  role VARCHAR(5) DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
  
  CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3),
  CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Index for faster user lookup by email (used in authentication)
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

COMMENT ON TABLE users IS 'Stores user account information with authentication details and profile data';
COMMENT ON COLUMN users.password IS 'BCrypt hashed password for security';
COMMENT ON COLUMN users.bio IS 'User biography/about section';
COMMENT ON COLUMN users.website IS 'User personal website URL';
COMMENT ON COLUMN users.location IS 'User location (city, country)';

-- ================================================================
-- TABLE: categories
-- Description: Stores blog post categories
-- ================================================================
CREATE TABLE categories (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  slug VARCHAR(100) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  CONSTRAINT chk_category_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
  CONSTRAINT chk_slug_format CHECK (slug ~* '^[a-z0-9-]+$')
);

-- Index for faster category lookup
CREATE INDEX idx_categories_name ON categories(name);
CREATE INDEX idx_categories_slug ON categories(slug);

COMMENT ON TABLE categories IS 'Stores blog post categories';
COMMENT ON COLUMN categories.slug IS 'URL-friendly version of category name';

-- ================================================================
-- TABLE: posts
-- Description: Stores blog posts with metadata
-- ================================================================
CREATE TABLE posts (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL,
  category_id INTEGER,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  excerpt VARCHAR(500),
  status VARCHAR(20) DEFAULT 'draft' CHECK (status IN ('draft', 'published', 'archived')),
  cover_image VARCHAR(500),
  views INTEGER DEFAULT 0,
  likes_count INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
  
  CONSTRAINT chk_title_not_empty CHECK (LENGTH(TRIM(title)) > 0),
  CONSTRAINT chk_content_not_empty CHECK (LENGTH(TRIM(content)) > 0),
  CONSTRAINT chk_views_positive CHECK (views >= 0),
  CONSTRAINT chk_likes_positive CHECK (likes_count >= 0)
);

-- Performance indexes for frequent queries
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_category_id ON posts(category_id);
CREATE INDEX idx_posts_status ON posts(status);
CREATE INDEX idx_posts_title ON posts(title);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);

COMMENT ON TABLE posts IS 'Stores blog posts with full content and metadata';
COMMENT ON COLUMN posts.status IS 'Post publication status: draft, published, or archived';
COMMENT ON COLUMN posts.cover_image IS 'Cover/banner image for the post';
COMMENT ON COLUMN posts.category_id IS 'Primary category for the post (nullable)';
COMMENT ON COLUMN posts.likes_count IS 'Number of likes/favorites for the post';

-- ================================================================
-- TABLE: tags
-- Description: Stores tags for categorizing posts
-- ================================================================
CREATE TABLE tags (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  slug VARCHAR(100) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  CONSTRAINT chk_tag_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
  CONSTRAINT chk_tag_slug_format CHECK (slug ~* '^[a-z0-9-]+$')
);

-- Index for faster tag lookup
CREATE INDEX idx_tags_name ON tags(name);
CREATE INDEX idx_tags_slug ON tags(slug);

COMMENT ON TABLE tags IS 'Stores tags for post categorization';
COMMENT ON COLUMN tags.slug IS 'URL-friendly version of tag name';

-- ================================================================
-- TABLE: post_tags (Junction Table)
-- Description: Many-to-many relationship between posts and tags
-- ================================================================
CREATE TABLE post_tags (
  post_id INTEGER NOT NULL,
  tag_id INTEGER NOT NULL,
  
  PRIMARY KEY (post_id, tag_id),
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Indexes for efficient tag filtering
CREATE INDEX idx_post_tags_post_id ON post_tags(post_id);
CREATE INDEX idx_post_tags_tag_id ON post_tags(tag_id);

COMMENT ON TABLE post_tags IS 'Junction table for many-to-many relationship between posts and tags';

-- ================================================================
-- TABLE: post_categories (Junction Table)
-- Description: Many-to-many relationship between posts and categories
-- ================================================================
CREATE TABLE post_categories (
  post_id INTEGER NOT NULL,
  category_id INTEGER NOT NULL,
  
  PRIMARY KEY (post_id, category_id),
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Indexes for efficient category filtering
CREATE INDEX idx_post_categories_post_id ON post_categories(post_id);
CREATE INDEX idx_post_categories_category_id ON post_categories(category_id);

COMMENT ON TABLE post_categories IS 'Junction table for many-to-many relationship between posts and categories';

-- ================================================================
-- TABLE: comments (PostgreSQL Implementation)
-- Description: Stores user comments on posts
-- Note: MongoDB alternative also available (CommentMongoDAO)
-- ================================================================
CREATE TABLE comments (
  id SERIAL PRIMARY KEY,
  post_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(20) DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  
  CONSTRAINT chk_comment_not_empty CHECK (LENGTH(TRIM(content)) > 0),
  CONSTRAINT chk_comment_length CHECK (LENGTH(content) <= 5000)
);

-- Indexes for efficient comment retrieval
CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_status ON comments(status);
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);

COMMENT ON TABLE comments IS 'Stores user comments on blog posts (PostgreSQL implementation - MongoDB alternative available)';
COMMENT ON COLUMN comments.status IS 'Comment status: PENDING, APPROVED, or REJECTED';

-- ================================================================
-- TABLE: reviews (PostgreSQL Implementation)
-- Description: Stores user reviews/ratings for posts
-- Note: MongoDB alternative also available (ReviewMongoDAO)
-- ================================================================
CREATE TABLE reviews (
  id SERIAL PRIMARY KEY,
  post_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL,
  rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
  title VARCHAR(255),
  content TEXT,
  helpful BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  
  CONSTRAINT chk_review_length CHECK (LENGTH(content) <= 5000),
  UNIQUE (post_id, user_id) -- One review per user per post
);

-- Indexes for efficient review queries
CREATE INDEX idx_reviews_post_id ON reviews(post_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_helpful ON reviews(helpful);

COMMENT ON TABLE reviews IS 'Stores user reviews and ratings for blog posts (PostgreSQL implementation - MongoDB alternative available)';
COMMENT ON COLUMN reviews.rating IS 'Star rating from 1 to 5';
COMMENT ON COLUMN reviews.helpful IS 'Marks reviews that were marked as helpful';

