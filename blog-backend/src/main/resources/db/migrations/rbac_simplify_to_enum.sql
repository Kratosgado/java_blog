-- Lab 7: Simplify RBAC - Replace Role table with enum column
-- Migrates from many-to-many relationship to simple enum column
--
-- Add NOT NULL constraint
ALTER TABLE users ALTER COLUMN role SET NOT NULL;
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;

-- Add CHECK constraint to ensure only valid enum values
ALTER TABLE users
ADD CONSTRAINT check_user_role
CHECK (role IN ('ADMIN', 'AUTHOR', 'READER'));

-- Create index on role column for better query performance
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- ============================================================================
-- 5. Create helpful view for user summary
-- ============================================================================

CREATE OR REPLACE VIEW user_summary AS
SELECT
    u.id AS user_id,
    u.username,
    u.email,
    u.auth_provider,
    u.role,
    CASE
        WHEN u.role = 'ADMIN' THEN 'Administrator'
        WHEN u.role = 'AUTHOR' THEN 'Author'
        WHEN u.role = 'READER' THEN 'Reader'
    END AS role_display_name
FROM users u;

