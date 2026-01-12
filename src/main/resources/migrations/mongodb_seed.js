// MongoDB Seed Data for blog_nosql database
// Run with: mongosh blog_nosql < mongodb_seed.js

use blog_nosql;

// Drop and recreate collections
db.comments.drop();
db.reviews.drop();

// Seed comments
db.comments.insertMany([
  {
    post_id: 1, user_id: 2, content: "Great JavaFX tutorial!",
    author_name: "Jane Smith", author_avatar_url: "https://i.pravatar.cc/150?img=2",
    status: "APPROVED", parent_id: null, depth: 0,
    reactions: { like: 15, love: 3 }, mentions: [], attachments: [],
    metadata: { platform: "JavaFX" },
    created_at: new Date("2026-01-10T08:30:00Z"),
    updated_at: new Date("2026-01-10T08:30:00Z")
  }
]);

// Seed reviews
db.reviews.insertMany([
  {
    post_id: 1, user_id: 2, rating: 5,
    title: "Perfect for Beginners",
    content: "Exactly what I needed!",
    helpful: true, author_name: "Jane Smith",
    author_avatar_url: "https://i.pravatar.cc/150?img=2",
    images: [], votes: { helpful: 25, not_helpful: 2 },
    metadata: { platform: "JavaFX", version: "1.0" },
    created_at: new Date("2026-01-10T12:00:00Z"),
    updated_at: new Date("2026-01-10T12:00:00Z")
  }
]);

// Create indexes
db.comments.createIndex({ post_id: 1 });
db.comments.createIndex({ user_id: 1 });
db.reviews.createIndex({ post_id: 1 });
db.reviews.createIndex({ user_id: 1 });
db.reviews.createIndex({ rating: -1 });

print("✓ MongoDB seeded successfully!");
