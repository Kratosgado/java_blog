# NoSQL Design Document

## Smart Blogging Platform - MongoDB Alternative Design

**Version**: 2.0  
**Type**: Optional Implementation  
**Date**: January 2026

---

## Executive Summary

This document presents an alternative NoSQL design for the Smart Blogging Platform using MongoDB. While the primary implementation uses PostgreSQL (SQL), this NoSQL design demonstrates how to handle unstructured data such as user reviews, comments, and analytics logs.

**Key Benefits of NoSQL Approach**:
- Flexible schema for varying comment structures
- Better performance for unstructured log data
- Easier horizontal scaling
- Natural nesting of related data

---

## 1. NoSQL Use Cases in Blogging Platform

### 1.1 Ideal NoSQL Candidates

#### 1. Comments & Reviews (Flexible Structure)
```
Why NoSQL:
- Comments vary in structure (text, images, ratings, metadata)
- Nested replies create variable depth
- Need to store rich metadata (edited_count, flags, votes)
- Frequent schema additions (new comment features)

SQL Alternative: Complex JOINs, normalized tables
NoSQL Solution: Cleaner, more flexible document model
```

#### 2. User Activity Logs (Time-Series Data)
```
Why NoSQL:
- High-volume write-heavy workload
- Variable data fields per event
- Time-based partitioning benefits
- Temporal analytics queries

SQL Alternative: Would require large single table
NoSQL Solution: Collections partitioned by date
```

#### 3. Post Metadata & Tags (Denormalization)
```
Why NoSQL:
- Frequently co-accessed data
- Variable number of tags per post
- Quick aggregations (tag counts)
- De-normalized storage optimal

SQL Alternative: Multiple JOINs required
NoSQL Solution: Embedded arrays
```

---

## 2. MongoDB Schema Design

### Collection 1: `comments` (Denormalized)

```javascript
db.comments.insertOne({
  _id: ObjectId("507f1f77bcf86cd799439011"),
  
  // Reference to post
  postId: ObjectId("507f1f77bcf86cd799439012"),
  
  // Commenter info (denormalized for performance)
  author: {
    userId: ObjectId("507f1f77bcf86cd799439013"),
    name: "John Doe",
    email: "john@example.com",
    avatar: "https://example.com/avatar.jpg"
  },
  
  // Comment content
  content: "Great post! Very informative.",
  
  // Nested replies (variable depth)
  replies: [
    {
      _id: ObjectId("507f1f77bcf86cd799439014"),
      author: {
        userId: ObjectId("507f1f77bcf86cd799439015"),
        name: "Jane Smith",
        avatar: "https://example.com/avatar2.jpg"
      },
      content: "Thanks for sharing!",
      createdAt: ISODate("2026-01-06T10:30:00Z"),
      votes: 5,
      flags: 0
    }
  ],
  
  // Metadata
  votes: 12,
  flags: 0,
  isDeleted: false,
  editHistory: [
    {
      content: "Original content",
      editedAt: ISODate("2026-01-06T10:00:00Z"),
      editedBy: ObjectId("507f1f77bcf86cd799439013")
    }
  ],
  
  // Timestamps
  createdAt: ISODate("2026-01-06T10:00:00Z"),
  updatedAt: ISODate("2026-01-06T10:20:00Z")
})
```

**Advantages**:
- Nested replies stored naturally
- No complex JOINs for comment threads
- Flexible metadata storage
- Edit history preserved

**Disadvantages**:
- Data duplication (author info)
- Update complexity (propagate author changes)
- Could grow large (unbounded replies)

---

### Collection 2: `activityLogs` (Time-Series)

```javascript
db.activityLogs.insertOne({
  _id: ObjectId("507f1f77bcf86cd799439020"),
  
  // User who performed action
  userId: ObjectId("507f1f77bcf86cd799439013"),
  username: "john_doe",
  
  // Action details (flexible schema)
  action: "view_post",
  actionDetails: {
    postId: ObjectId("507f1f77bcf86cd799439012"),
    postTitle: "Java Performance Tips",
    timeSpent: 125, // seconds
    scrollDepth: 0.85
  },
  
  // Device info (variable fields)
  device: {
    type: "desktop",
    browser: "Chrome",
    version: "120.0",
    os: "Linux"
  },
  
  // Geographic info
  location: {
    country: "USA",
    city: "San Francisco",
    ip: "192.168.1.1"
  },
  
  // Timestamp for time-series
  timestamp: ISODate("2026-01-06T14:30:00Z"),
  
  // TTL index (auto-expire after 90 days)
  createdAt: ISODate("2026-01-06T14:30:00Z")
})

// Create TTL index
db.activityLogs.createIndex(
  { createdAt: 1 },
  { expireAfterSeconds: 7776000 } // 90 days
)
```

**Advantages**:
- Natural fit for time-series data
- Built-in TTL for auto-expiration
- Flexible event types (view, click, comment, share)
- Efficient querying by timestamp

**Disadvantages**:
- High storage (history-heavy)
- Requires sharding for large scale
- Data duplication opportunities

---

### Collection 3: `reviews` (Nested Documents)

```javascript
db.reviews.insertOne({
  _id: ObjectId("507f1f77bcf86cd799439030"),
  
  // What's being reviewed
  postId: ObjectId("507f1f77bcf86cd799439012"),
  type: "post",
  
  // Reviewer info
  reviewer: {
    userId: ObjectId("507f1f77bcf86cd799439013"),
    name: "John Doe",
    reputation: 150
  },
  
  // Rating & review content
  rating: 4.5, // 0-5 stars
  title: "Excellent guide!",
  content: "Very comprehensive and well-written post about Java performance.",
  
  // Rich feedback (flexible structure)
  feedback: {
    helpfulness: {
      helpful: 18,
      notHelpful: 2
    },
    accuracy: {
      accurate: 20,
      inaccurate: 0
    },
    clarity: {
      clear: 19,
      unclear: 1
    },
    customAttributes: {
      codeQuality: 5,
      explanationDepth: 4,
      practicalRelevance: 5
    }
  },
  
  // Media attachments (images, code samples)
  attachments: [
    {
      type: "image",
      url: "https://example.com/screenshot.png",
      caption: "Performance benchmark"
    },
    {
      type: "code",
      language: "java",
      snippet: "public void optimizeQuery() { ... }",
      fileSize: 250
    }
  ],
  
  // Moderation
  status: "approved",
  moderatedBy: ObjectId("507f1f77bcf86cd799439031"),
  moderationNotes: "Verified and helpful",
  
  // Temporal data
  createdAt: ISODate("2026-01-06T12:00:00Z"),
  updatedAt: ISODate("2026-01-06T14:30:00Z"),
  deletedAt: null // Soft delete
})
```

---

### Collection 4: `postAnalytics` (Aggregated Data)

```javascript
db.postAnalytics.insertOne({
  _id: ObjectId("507f1f77bcf86cd799439040"),
  
  // Associated post
  postId: ObjectId("507f1f77bcf86cd799439012"),
  
  // Daily aggregates
  date: ISODate("2026-01-06"),
  
  metrics: {
    views: {
      total: 1250,
      unique: 850,
      bySource: {
        direct: 400,
        search: 350,
        social: 300,
        referral: 200
      }
    },
    
    engagement: {
      likes: 45,
      shares: 12,
      comments: 8,
      bookmarks: 23,
      avgTimeOnPage: 145 // seconds
    },
    
    demographics: {
      byCountry: {
        USA: 500,
        UK: 150,
        India: 120,
        other: 80
      },
      byDeviceType: {
        desktop: 600,
        mobile: 200,
        tablet: 50
      }
    },
    
    sentiment: {
      positive: 38,
      neutral: 15,
      negative: 2,
      avgSentimentScore: 0.82
    }
  },
  
  // Growth metrics
  growth: {
    viewsChange: 1.25, // 25% change from previous day
    engagementChange: 0.95,
    trendingScore: 0.78
  }
})

// Time-series optimized index
db.postAnalytics.createIndex({ postId: 1, date: -1 })
```

---

## 3. Indexing Strategy for MongoDB

### Critical Indexes

```javascript
// Comments collection
db.comments.createIndex({ postId: 1, createdAt: -1 })    // Query recent comments
db.comments.createIndex({ "author.userId": 1 })          // User's comments
db.comments.createIndex({ createdAt: 1 }, { ttl: 0 })    // Sort by date

// Activity Logs collection
db.activityLogs.createIndex({ userId: 1, timestamp: -1 }) // User activity timeline
db.activityLogs.createIndex({ action: 1, timestamp: -1 }) // Activity type analysis
db.activityLogs.createIndex({ timestamp: 1 }, { expireAfterSeconds: 7776000 }) // TTL

// Reviews collection
db.reviews.createIndex({ postId: 1, rating: -1 })        // Reviews by rating
db.reviews.createIndex({ "reviewer.userId": 1 })         // User reviews
db.reviews.createIndex({ status: 1, createdAt: -1 })     // Moderation queue

// Post Analytics collection
db.postAnalytics.createIndex({ postId: 1, date: -1 })    // Daily metrics
db.postAnalytics.createIndex({ "metrics.engagement.comments": -1 }) // Popular posts
```

---

## 4. Query Patterns in MongoDB

### Query 1: Get Post Comments with Replies

```javascript
// Single query (MongoDB advantage over SQL)
db.comments
  .find({ postId: ObjectId("507f1f77bcf86cd799439012") })
  .sort({ createdAt: -1 })
  .limit(10)
```

**SQL Equivalent** (Multiple JOINs needed):
```sql
SELECT c.*, r.* FROM comments c
LEFT JOIN replies r ON c.id = r.comment_id
WHERE c.post_id = ? AND c.deleted = false
ORDER BY c.created_at DESC
LIMIT 10
```

**Advantage**: MongoDB returns nested structure naturally

---

### Query 2: Time-Series Analytics

```javascript
// Get daily analytics for last 30 days
db.postAnalytics
  .find({
    postId: ObjectId("507f1f77bcf86cd799439012"),
    date: {
      $gte: ISODate("2025-12-07"),
      $lte: ISODate("2026-01-06")
    }
  })
  .sort({ date: -1 })
```

**SQL Equivalent**:
```sql
SELECT date, metrics FROM post_analytics
WHERE post_id = ? AND date BETWEEN ? AND ?
ORDER BY date DESC
```

**Advantage**: Flexible metrics structure, easier to add new fields

---

### Query 3: Aggregation Pipeline (Group by)

```javascript
// Find trending posts by engagement
db.postAnalytics
  .aggregate([
    {
      $match: {
        date: { $gte: ISODate("2026-01-01") }
      }
    },
    {
      $group: {
        _id: "$postId",
        avgViews: { $avg: "$metrics.views.total" },
        totalEngagement: { $sum: "$metrics.engagement.comments" },
        avgSentiment: { $avg: "$metrics.sentiment.avgSentimentScore" }
      }
    },
    {
      $sort: { totalEngagement: -1 }
    },
    {
      $limit: 10
    }
  ])
```

---

## 5. Data Consistency Strategies

### Denormalization Trade-offs

#### Comments with Author Info (Denormalized)
```javascript
{
  author: {
    userId: ObjectId(...),
    name: "John Doe",     // Duplicated
    avatar: "..."         // Duplicated
  }
}
```

**Consistency Maintenance**:
```javascript
// When user updates profile
db.comments.updateMany(
  { "author.userId": ObjectId(...) },
  {
    $set: {
      "author.name": "New Name",
      "author.avatar": "new_avatar.jpg"
    }
  }
)
```

**Trade-off**: Denormalization for read speed, update complexity for data changes

---

## 6. Transactions (Multi-Document)

### Example: Create Comment with Analytics Update

```javascript
// MongoDB 4.0+ supports multi-document transactions
const session = db.getMongo().startSession()

session.startTransaction()
try {
  // Insert comment
  const comment = {
    postId: ObjectId(...),
    author: { ... },
    content: "Great post!",
    createdAt: new Date()
  }
  const commentResult = db.comments.insertOne(comment)
  
  // Update post analytics
  db.posts.updateOne(
    { _id: ObjectId(...) },
    {
      $inc: {
        "stats.commentCount": 1,
        "stats.lastCommentDate": new Date()
      }
    }
  )
  
  session.commitTransaction()
} catch (error) {
  session.abortTransaction()
  throw error
} finally {
  session.endSession()
}
```

---

## 7. Backup & Recovery (MongoDB)

### Backup Strategy

```bash
# Full backup using mongodump
mongodump --db blogging_platform --out ./backup

# Backup to tar.gz
mongodump --db blogging_platform --archive=backup.archive.gz --gzip

# Incremental using oplog
mongodump --db blogging_platform --oplog
```

### Restore

```bash
# Full restore
mongorestore ./backup

# From archive
mongorestore --archive=backup.archive.gz --gzip

# Point-in-time restore (using oplog)
mongorestore --archive=backup.archive --oplogReplay
```

---

## 8. Horizontal Scaling (Sharding)

### Shard Key Selection

```javascript
// Shard by userId for comments
// Good: Distribution, query patterns, growth
sh.shardCollection("blog.comments", { "author.userId": 1 })

// Shard by timestamp for activity logs
sh.shardCollection("blog.activityLogs", { timestamp: 1 })

// Shard by postId for analytics
sh.shardCollection("blog.postAnalytics", { postId: 1 })
```

**Sharding Architecture**:
```
┌─────────────────────────────────┐
│   Application Layer             │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│   Mongos Router (Sharding)      │
└─┬───────────────────────────┬──┘
  │                           │
┌─▼──────────────┐  ┌───────▼─┐
│  Shard 1       │  │ Shard 2 │
│  (UserA-M)     │  │ (UserN-Z)│
└────────────────┘  └─────────┘
```

---

## 9. Implementation Comparison

### MongoDB vs PostgreSQL

| Aspect | MongoDB | PostgreSQL |
|--------|---------|-----------|
| **Schema Flexibility** | ✓ High | Limited |
| **Nested Data** | ✓ Native | Complex JOINs |
| **Write Speed** | ✓ High | Standard |
| **Query Flexibility** | ✓ High | Structured |
| **ACID Transactions** | ✓ Multi-doc (4.0+) | ✓ Full support |
| **Horizontal Scaling** | ✓ Sharding | Need 3rd party |
| **Complex Queries** | Limited | ✓ Excellent |
| **Data Relationships** | Denormalization | ✓ Normalization |
| **Storage Size** | Higher | Lower |
| **Learning Curve** | Easier | Steeper |

---

## 10. Hybrid Approach (Recommended)

### Optimal Architecture

```
┌─────────────────────────────────────┐
│      JavaFX Application Layer       │
└──────┬──────────────────┬───────────┘
       │                  │
       ▼                  ▼
  ┌────────────┐    ┌──────────────┐
  │PostgreSQL  │    │   MongoDB    │
  │(Relational)│    │(Unstructured)│
  └────────────┘    └──────────────┘
       │                  │
  • Posts          • Comments
  • Users          • Reviews
  • Tags           • Activity Logs
  • Comments       • Analytics
  • Relationships  • Time-series
```

### Data Distribution Rules

**PostgreSQL** (OLTP - Transactional):
- Core entities (Posts, Users, Tags)
- Structured queries with JOINs
- Financial/critical data
- Frequent updates

**MongoDB** (OLAP - Analytical):
- Unstructured feedback (comments, reviews)
- Time-series data (activity logs, analytics)
- Variable schema (events, metrics)
- Append-heavy workloads

---

## 11. Migration Path (If Adopting NoSQL)

### Phase 1: Start with PostgreSQL (Current ✓)
- Single database
- Full ACID compliance
- No operational complexity

### Phase 2: Add MongoDB for New Features
- Activity logs → MongoDB
- Reviews → MongoDB
- Keep core data in PostgreSQL

### Phase 3: Hybrid Optimization
- Both databases fully utilized
- Separate read replicas if needed
- Event streaming between them

### Phase 4: Full NoSQL (Optional)
- Migrate posts to MongoDB if needed
- Complex sharding setup
- Advanced replication

---

## 12. Monitoring & Operations

### MongoDB Operations

```javascript
// Check database size
db.stats()

// Collection-level stats
db.comments.stats()

// Index usage
db.comments.aggregate([{ $indexStats: {} }])

// Slow query log
db.setProfilingLevel(1, { slowms: 100 })
```

---

## Conclusion

**MongoDB is ideal for**:
- ✓ Comments with nested replies
- ✓ Activity logs (time-series)
- ✓ Reviews with rich metadata
- ✓ Analytics aggregations

**PostgreSQL remains ideal for**:
- ✓ Core entities (Posts, Users, Tags)
- ✓ Complex transactions
- ✓ Data relationships
- ✓ Financial accuracy

**Recommendation**: **Hybrid approach** - Use PostgreSQL for transactional data, MongoDB for analytical/unstructured data. This provides:
- Best query performance
- Data consistency
- Scalability
- Operational simplicity

---

**Implementation Status**: Design Complete ✓  
**Production Readiness**: Ready for implementation  
**Complexity Level**: Medium (requires two database systems)

For questions or detailed implementation guidance, refer to the README.md and main DATABASE_DESIGN.md documents.
