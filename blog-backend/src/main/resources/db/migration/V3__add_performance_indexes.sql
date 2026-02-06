-- Performance optimization indexes for blog platform
-- These indexes significantly improve query performance for common operations

-- Composite index for filtering and sorting published posts
CREATE INDEX IF NOT EXISTS idx_posts_status_created_at
ON posts(status, created_at DESC);

-- Composite index for trending posts (status + views)
CREATE INDEX IF NOT EXISTS idx_posts_status_views
ON posts(status, views DESC);

-- Composite index for user's published posts
CREATE INDEX IF NOT EXISTS idx_posts_user_status
ON posts(user_id, status);

-- Composite index for category's published posts
CREATE INDEX IF NOT EXISTS idx_posts_category_status
ON posts(category_id, status);

-- Full-text search index using tsvector for title and content
-- This dramatically improves search performance
CREATE INDEX IF NOT EXISTS idx_posts_title_content_fts
ON posts USING gin(to_tsvector('english', title || ' ' || COALESCE(content, '')));

-- Index for case-insensitive title searches
CREATE INDEX IF NOT EXISTS idx_posts_title_lower
ON posts(LOWER(title));

-- Partial index for published posts only (reduces index size)
CREATE INDEX IF NOT EXISTS idx_posts_published_created_at
ON posts(created_at DESC) WHERE status = 'published';

-- Index for post_tags junction table
CREATE INDEX IF NOT EXISTS idx_post_tags_post_id
ON post_tags(post_id);

CREATE INDEX IF NOT EXISTS idx_post_tags_tag_id
ON post_tags(tag_id);

-- Covering index for post list queries (includes commonly selected columns)
CREATE INDEX IF NOT EXISTS idx_posts_list_covering
ON posts(status, created_at DESC)
INCLUDE (id, title, slug, excerpt, user_id, category_id, views, cover_image);

-- Analyze tables to update statistics for query planner
ANALYZE posts;
ANALYZE post_tags;
ANALYZE users;
ANALYZE categories;
ANALYZE tags;
