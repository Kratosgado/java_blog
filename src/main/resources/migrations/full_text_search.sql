-- Full-Text Search Enhancement for Posts
-- This migration adds PostgreSQL full-text search capabilities

-- Add search vector column to posts table
ALTER TABLE posts ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- Create GIN index for full-text search (faster than GiST for static data)
CREATE INDEX IF NOT EXISTS idx_posts_search_vector ON posts USING GIN(search_vector);

-- Create function to update search vector
CREATE OR REPLACE FUNCTION posts_search_vector_update() RETURNS trigger AS $$
BEGIN
  NEW.search_vector :=
    setweight(to_tsvector('english', COALESCE(NEW.title, '')), 'A') ||
    setweight(to_tsvector('english', COALESCE(NEW.content, '')), 'B') ||
    setweight(to_tsvector('english', COALESCE(NEW.excerpt, '')), 'C');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update search vector
DROP TRIGGER IF EXISTS posts_search_vector_trigger ON posts;
CREATE TRIGGER posts_search_vector_trigger
BEFORE INSERT OR UPDATE ON posts
FOR EACH ROW EXECUTE FUNCTION posts_search_vector_update();

-- Update existing posts with search vectors
UPDATE posts SET search_vector =
  setweight(to_tsvector('english', COALESCE(title, '')), 'A') ||
  setweight(to_tsvector('english', COALESCE(content, '')), 'B') ||
  setweight(to_tsvector('english', COALESCE(excerpt, '')), 'C')
WHERE search_vector IS NULL;

-- Create view for search results with ranking
CREATE OR REPLACE VIEW post_search_results AS
SELECT
  p.id,
  p.user_id,
  p.title,
  p.content,
  p.excerpt,
  p.status,
  p.created_at,
  p.updated_at,
  p.views,
  p.featured_image,
  p.cover_image,
  p.icon,
  p.author_name,
  p.author_avatar_url,
  p.search_vector,
  ts_rank(p.search_vector, query) AS rank
FROM posts p, to_tsquery('english', '') AS query
WHERE p.search_vector @@ query
ORDER BY rank DESC;

COMMENT ON TABLE posts IS 'Blog posts with full-text search support';
COMMENT ON COLUMN posts.search_vector IS 'Full-text search vector (auto-updated via trigger)';
COMMENT ON INDEX idx_posts_search_vector IS 'GIN index for fast full-text search on posts';
