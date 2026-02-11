-- Performance optimization indexes for blog platform
-- These indexes significantly improve query performance for common operations

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
-- Add the stored generated column
-- ALTER TABLE posts DROP COLUMN search_vector;

ALTER TABLE posts 
ADD COLUMN  search_vector tsvector
GENERATED ALWAYS AS (
    setweight(to_tsvector('english', title), 'A') || 
    setweight(to_tsvector('english', COALESCE(content, '')), 'B')
) STORED;
-- Create the GIN index on the stored column
CREATE INDEX IF NOT EXISTS idx_posts_fts_vector 
ON posts USING GIN(search_vector);

-- Partial index for published posts only (reduces index size)
CREATE INDEX IF NOT EXISTS idx_posts_published_created_at
ON posts(created_at DESC) WHERE status = 'published';

-- Analyze tables to update statistics for query planner
ANALYZE posts;
ANALYZE post_tags;
ANALYZE users;
ANALYZE categories;
ANALYZE tags;
