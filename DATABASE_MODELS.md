# Database Models - Java Blog Platform

This document contains the conceptual, logical, and physical database models for the Java Blog Platform using Mermaid diagrams.

## Table of Contents

- [Conceptual Model](#conceptual-model)
- [Logical Model](#logical-model)
- [Physical Model](#physical-model)
- [Key Design Highlights](#key-design-highlights)

---

## Conceptual Model

The conceptual model represents the high-level business entities and their relationships.

```mermaid
erDiagram
    USER ||--o{ POST : "creates"
    USER ||--o{ COMMENT : "writes"
    USER ||--o{ REVIEW : "submits"

    POST ||--o{ COMMENT : "has"
    POST ||--o{ REVIEW : "has"
    POST }o--|| CATEGORY : "belongs to"
    POST }o--o{ TAG : "tagged with"
    POST }o--o{ CATEGORY : "categorized in"

    USER {

    }

    POST {
    }

    CATEGORY {
    }

    TAG {
    }

    COMMENT {
    }

    REVIEW {
    }
```

### Conceptual Model Features

- **Entities**: User, Post, Category, Tag, Comment, Review
- **Relationships**:
  - Users create Posts (1:N)
  - Users write Comments (1:N)
  - Users submit Reviews (1:N)
  - Posts have Comments (1:N)
  - Posts have Reviews (1:N)
  - Posts belong to a Category (N:1)
  - Posts are tagged with Tags (N:M)
  - Posts are categorized in Categories (N:M)

---

## Logical Model

The logical model shows the normalized database structure with primary keys, foreign keys, and data types.

```mermaid
erDiagram
    users ||--o{ posts : "user_id"
    users ||--o{ comments : "user_id"
    users ||--o{ reviews : "user_id"

    posts ||--o{ comments : "post_id"
    posts ||--o{ reviews : "post_id"
    posts }o--|| categories : "category_id"
    posts }o--o{ tags : "post_tags"
    posts }o--o{ categories : "post_categories"

    post_tags }|--|| posts : "post_id"
    post_tags }|--|| tags : "tag_id"

    post_categories }|--|| posts : "post_id"
    post_categories }|--|| categories : "category_id"

    users {
        int id PK
        varchar username UK
        varchar password
        varchar email UK
        varchar avatar_url
        text bio
        varchar website
        varchar location
        timestamp created_at
    }

    posts {
        int id PK
        int user_id FK
        int category_id FK
        varchar title
        text content
        varchar excerpt
        varchar status
        varchar cover_image
        int views
        int likes_count
        timestamp created_at
        timestamp updated_at
    }

    categories {
        int id PK
        varchar name UK
        varchar slug UK
        text description
        timestamp created_at
    }

    tags {
        int id PK
        varchar name UK
        varchar slug UK
        text description
        timestamp created_at
    }

    post_tags {
        int post_id PK_FK
        int tag_id PK_FK
    }

    post_categories {
        int post_id PK_FK
        int category_id PK_FK
    }

    comments {
        int id PK
        int post_id FK
        int user_id FK
        text content
        varchar status
        timestamp created_at
        timestamp updated_at
    }

    reviews {
        int id PK
        int post_id FK
        int user_id FK
        int rating
        varchar title
        text content
        boolean helpful
        timestamp created_at
        timestamp updated_at
    }
```

### Logical Model Features

- **Normalization**: Third Normal Form (3NF)
- **Junction Tables**:
  - `post_tags` for Post-Tag many-to-many relationship
  - `post_categories` for Post-Category many-to-many relationship
- **Keys**:
  - PK = Primary Key
  - FK = Foreign Key
  - UK = Unique Key
- **Total Tables**: 8 (6 entity tables + 2 junction tables)

---

## Physical Model

The physical model represents the actual PostgreSQL database implementation with constraints, indexes, and triggers.

```mermaid
erDiagram
    users ||--o{ posts : "ON DELETE CASCADE"
    users ||--o{ comments : "ON DELETE CASCADE"
    users ||--o{ reviews : "ON DELETE CASCADE"

    posts ||--o{ comments : "ON DELETE CASCADE"
    posts ||--o{ reviews : "ON DELETE CASCADE"
    posts }o--o| categories : "ON DELETE SET NULL"

    posts }o--o{ tags : "post_tags M:N"
    posts }o--o{ categories : "post_categories M:N"

    post_tags }|--|| posts : "ON DELETE CASCADE"
    post_tags }|--|| tags : "ON DELETE CASCADE"

    post_categories }|--|| posts : "ON DELETE CASCADE"
    post_categories }|--|| categories : "ON DELETE CASCADE"

    users {
        SERIAL id PK
        VARCHAR_50 username UK
        VARCHAR_255 password
        VARCHAR_100 email UK
        VARCHAR_500 avatar_url
        TEXT bio
        VARCHAR_255 website
        VARCHAR_100 location
        TIMESTAMP created_at
    }

    categories {
        SERIAL id PK
        VARCHAR_100 name UK
        VARCHAR_100 slug UK
        TEXT description
        TIMESTAMP created_at
    }

    posts {
        SERIAL id PK
        INTEGER user_id FK
        INTEGER category_id FK
        VARCHAR_255 title
        TEXT content
        VARCHAR_500 excerpt
        VARCHAR_20 status
        VARCHAR_500 cover_image
        INTEGER views
        INTEGER likes_count
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    tags {
        SERIAL id PK
        VARCHAR_100 name UK
        VARCHAR_100 slug UK
        TEXT description
        TIMESTAMP created_at
    }

    post_tags {
        INTEGER post_id PK
        INTEGER tag_id PK
    }

    post_categories {
        INTEGER post_id PK
        INTEGER category_id PK
    }

    comments {
        SERIAL id PK
        INTEGER post_id FK
        INTEGER user_id FK
        TEXT content
        VARCHAR_20 status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    reviews {
        SERIAL id PK
        INTEGER post_id FK
        INTEGER user_id FK
        INTEGER rating
        VARCHAR_255 title
        TEXT content
        BOOLEAN helpful
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
```

### Physical Model Features

#### Database Management System

- **DBMS**: PostgreSQL 17.2+
- **Architecture**: Hybrid (PostgreSQL + MongoDB for comments/reviews)

#### Indexes (25+ total)

- `idx_users_email`, `idx_users_username`
- `idx_posts_user_id`, `idx_posts_category_id`, `idx_posts_status`, `idx_posts_title`, `idx_posts_created_at`
- `idx_categories_name`, `idx_categories_slug`
- `idx_tags_name`, `idx_tags_slug`
- `idx_post_tags_post_id`, `idx_post_tags_tag_id`
- `idx_post_categories_post_id`, `idx_post_categories_category_id`
- `idx_comments_post_id`, `idx_comments_user_id`, `idx_comments_status`, `idx_comments_created_at`
- `idx_reviews_post_id`, `idx_reviews_user_id`, `idx_reviews_rating`, `idx_reviews_helpful`

#### Constraints

- **CHECK Constraints**:
  - Username minimum 3 characters
  - Email format validation (regex)
  - Category/Tag slug format (lowercase-hyphen)
  - Post status must be 'draft', 'published', or 'archived'
  - Views and likes_count must be >= 0
  - Comment content 1-5000 characters
  - Review rating must be 1-5
- **UNIQUE Constraints**:
  - One review per user per post
  - Unique usernames and emails
  - Unique category and tag names/slugs
- **NOT NULL Constraints**: Applied to required fields

#### Triggers

```sql
-- Auto-update updated_at timestamp
- update_posts_updated_at
- update_comments_updated_at
- update_reviews_updated_at
```

#### Cascade Rules

- **ON DELETE CASCADE**: Users → Posts, Comments, Reviews
- **ON DELETE CASCADE**: Posts → Comments, Reviews
- **ON DELETE SET NULL**: Categories → Posts
- **ON DELETE CASCADE**: Junction tables (post_tags, post_categories)

#### Database Views

```sql
-- Post statistics with aggregated data
CREATE VIEW post_statistics AS ...

-- Popular posts ranked by views and likes
CREATE VIEW popular_posts AS ...
```

---

## Key Design Highlights

### 1. Normalization

- **Third Normal Form (3NF)** compliance
- No redundant data
- Atomic values in all columns
- Proper use of foreign keys

### 2. Scalability

- **25+ indexes** for query optimization
- **Views** for complex queries (post_statistics, popular_posts)
- **Junction tables** for many-to-many relationships
- Proper data types sized for expected data

### 3. Data Integrity

- **Foreign key constraints** ensure referential integrity
- **CHECK constraints** validate data ranges and formats
- **UNIQUE constraints** prevent duplicates
- **NOT NULL constraints** on required fields
- **Triggers** automatically maintain timestamp consistency
