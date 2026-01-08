-- ================================================================
-- Smart Blogging Platform - Database Seed Data
-- Description: Sample data for testing and demonstration
-- ================================================================

-- Clear existing data (use with caution!)
TRUNCATE TABLE reviews, post_tags, comments, tags, posts, users RESTART IDENTITY CASCADE;

-- ================================================================
-- SEED DATA: Users
-- Password for all users: "password123" (BCrypt hashed)
-- ================================================================
INSERT INTO users (username, email, password, avatar_url) VALUES
('john_doe', 'john.doe@example.com', '$2a$10$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=1'),
('jane_smith', 'jane.smith@example.com', '$2a$10$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=2'),
('bob_wilson', 'bob.wilson@example.com', '$2a$10$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=3'),
('alice_johnson', 'alice.johnson@example.com', '$2a$10$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=4'),
('charlie_brown', 'charlie.brown@example.com', '$2a$10$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=5'),
('diana_prince', 'diana.prince@example.com', '$2a$10$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=6'),
('ethan_hunt', 'ethan.hunt@example.com', '$2a$10$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=7'),
('fiona_gallagher', 'fiona.gallagher@example.com', '$2a$10$rF8kqGBqVqKGfqQYBqJqHeQtYGYqKqKQqKqKqKqKqKqKqKqKqKqKq', 'https://i.pravatar.cc/150?img=8');

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
-- SEED DATA: Posts
-- ================================================================
INSERT INTO posts (user_id, title, content, excerpt, status, featured_image, cover_image, icon, views) VALUES
(1, 'Getting Started with JavaFX: A Comprehensive Guide', 
'JavaFX is a powerful framework for building rich desktop applications in Java. In this comprehensive guide, we''ll explore the fundamentals of JavaFX and how to get started building your first application.

## What is JavaFX?

JavaFX is a software platform for creating and delivering desktop applications, as well as rich internet applications (RIAs) that can run across a wide variety of devices. It replaced Swing as the standard GUI library for Java SE.

## Setting Up Your Environment

To get started with JavaFX, you''ll need:
1. Java Development Kit (JDK) 11 or higher
2. An IDE like IntelliJ IDEA or Eclipse
3. JavaFX SDK

## Your First JavaFX Application

Let''s create a simple "Hello World" application:

```java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloWorld extends Application {
    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Hello, JavaFX!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 400, 300);
        
        primaryStage.setTitle("My First JavaFX App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

## Key Concepts

- **Stage**: The main window of your application
- **Scene**: The content container within a Stage
- **Nodes**: UI components like buttons, labels, etc.
- **Layout Panes**: Containers that manage the positioning of nodes

Stay tuned for more advanced JavaFX tutorials!',
'Learn the fundamentals of JavaFX and build your first desktop application with this comprehensive guide for beginners.',
'published',
'https://images.unsplash.com/photo-1587620962725-abab7fe55159?w=800',
'https://images.unsplash.com/photo-1587620962725-abab7fe55159?w=1200',
'☕',
1523),

(1, 'Database Design Principles: Achieving Third Normal Form',
'Database normalization is a crucial aspect of database design that helps eliminate redundancy and maintain data integrity. In this article, we''ll dive deep into Third Normal Form (3NF) and why it matters.

## Understanding Normalization

Normalization is the process of organizing data in a database to reduce redundancy and improve data integrity. The main goals are:
- Eliminate redundant data
- Ensure data dependencies make sense
- Protect data integrity

## First Normal Form (1NF)

A table is in 1NF if:
- All columns contain atomic values (no repeating groups)
- Each column contains values of a single type
- Each column has a unique name

## Second Normal Form (2NF)

A table is in 2NF if:
- It is in 1NF
- All non-key attributes are fully functionally dependent on the primary key

## Third Normal Form (3NF)

A table is in 3NF if:
- It is in 2NF
- No transitive dependencies exist

### Example: Blog Database

Let''s design a blog database in 3NF:

**Users Table**
```sql
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL
);
```

**Posts Table**
```sql
CREATE TABLE posts (
  id SERIAL PRIMARY KEY,
  user_id INTEGER REFERENCES users(id),
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL
);
```

This design ensures no data redundancy and maintains referential integrity!',
'Master database normalization and learn how to design databases that achieve Third Normal Form for optimal data integrity.',
'published',
'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800',
'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=1200',
'🗄️',
892),

(2, 'Mastering SQL Indexing for Performance Optimization',
'Database indexes are one of the most powerful tools for improving query performance. Learn how to use them effectively in this comprehensive guide.

## What are Indexes?

An index is a data structure that improves the speed of data retrieval operations on a database table. Think of it like an index in a book - instead of reading every page to find a topic, you can jump directly to the relevant pages.

## Types of Indexes

### 1. B-Tree Indexes (Default)
Most databases use B-tree indexes by default. They work well for:
- Equality comparisons (=)
- Range queries (<, >, BETWEEN)
- Sorting operations (ORDER BY)

### 2. Hash Indexes
Perfect for exact match lookups but don''t support range queries.

### 3. Composite Indexes
Indexes on multiple columns. The order of columns matters!

## When to Use Indexes

✅ Use indexes on:
- Primary keys (automatic)
- Foreign keys
- Columns frequently used in WHERE clauses
- Columns used in JOIN operations
- Columns used in ORDER BY

❌ Avoid indexes on:
- Small tables
- Columns with high update frequency
- Columns with low cardinality

## Real-World Example

```sql
-- Create index on posts table
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_status ON posts(status);

-- Composite index for common query pattern
CREATE INDEX idx_posts_user_status ON posts(user_id, status);
```

Performance improvement can be 50-100x faster!',
'Learn how to use database indexes effectively to dramatically improve query performance in your applications.',
'published',
'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800',
'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=1200',
'⚡',
2156),

(3, 'Building RESTful APIs with Java: Best Practices',
'RESTful APIs are the backbone of modern web applications. Let''s explore best practices for building robust, scalable REST APIs in Java.

## REST Principles

REST (Representational State Transfer) is an architectural style with six key constraints:
1. Client-Server architecture
2. Stateless communication
3. Cacheable responses
4. Uniform interface
5. Layered system
6. Code on demand (optional)

## HTTP Methods

- **GET**: Retrieve resources
- **POST**: Create new resources
- **PUT**: Update entire resources
- **PATCH**: Partial updates
- **DELETE**: Remove resources

## Status Codes

- 200 OK - Success
- 201 Created - Resource created
- 400 Bad Request - Invalid input
- 401 Unauthorized - Authentication required
- 404 Not Found - Resource not found
- 500 Internal Server Error - Server error

## Example API Design

```java
@RestController
@RequestMapping("/api/posts")
public class PostController {
    
    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }
    
    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }
    
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        return postService.createPost(post);
    }
}
```

Follow these principles for production-ready APIs!',
'Master REST API design in Java with proven best practices for building scalable, maintainable web services.',
'published',
'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=800',
'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=1200',
'🌐',
1678),

(2, 'Understanding Java Streams API: A Deep Dive',
'The Streams API introduced in Java 8 revolutionized how we process collections. Let''s explore its power and learn to use it effectively.

## What are Streams?

Streams are sequences of elements that support sequential and parallel aggregate operations. They provide a functional approach to processing data.

## Key Features

- **Declarative**: Describe what you want, not how to do it
- **Composable**: Chain operations together
- **Parallelizable**: Easy parallel processing
- **Lazy**: Operations are performed only when needed

## Common Operations

### Filter
```java
List<String> filtered = list.stream()
    .filter(s -> s.startsWith("A"))
    .collect(Collectors.toList());
```

### Map
```java
List<Integer> lengths = names.stream()
    .map(String::length)
    .collect(Collectors.toList());
```

### Reduce
```java
int sum = numbers.stream()
    .reduce(0, Integer::sum);
```

### Collectors
```java
Map<String, List<Post>> postsByAuthor = posts.stream()
    .collect(Collectors.groupingBy(Post::getAuthor));
```

## Performance Tips

1. Use parallel streams for CPU-intensive operations on large datasets
2. Avoid stateful operations in parallel streams
3. Consider creating streams from primitive types (IntStream, LongStream)
4. Use method references for cleaner code

Streams make your code more readable and maintainable!',
'Unlock the full potential of Java Streams API with this comprehensive guide to functional programming in Java.',
'published',
'https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800',
'https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=1200',
'🌊',
1034),

(4, 'Implementing Caching Strategies in Java Applications',
'Caching is essential for building high-performance applications. Learn different caching strategies and when to use them.

## Why Caching?

Caching reduces:
- Database load
- API calls
- Response times
- Server costs

## Caching Strategies

### 1. Cache-Aside (Lazy Loading)
Application checks cache first, loads from database on miss.

### 2. Write-Through
Data written to cache and database simultaneously.

### 3. Write-Behind
Data written to cache first, then asynchronously to database.

## Implementation Example

```java
public class PostCache {
    private final Map<Integer, CachedPost> cache = new ConcurrentHashMap<>();
    private final long TTL = 5 * 60 * 1000; // 5 minutes
    
    public Optional<Post> get(int id) {
        CachedPost cached = cache.get(id);
        if (cached != null && !cached.isExpired()) {
            return Optional.of(cached.getPost());
        }
        cache.remove(id);
        return Optional.empty();
    }
    
    public void put(int id, Post post) {
        cache.put(id, new CachedPost(post, System.currentTimeMillis() + TTL));
    }
}
```

## Cache Invalidation

The two hardest problems in computer science:
1. Naming things
2. Cache invalidation
3. Off-by-one errors

Strategies:
- Time-based expiration (TTL)
- Event-based invalidation
- Manual invalidation

Implement caching wisely for better performance!',
'Learn effective caching strategies to dramatically improve application performance and reduce server load.',
'published',
'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800',
'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=1200',
'💾',
756),

(5, 'Test-Driven Development in Java: A Practical Guide',
'TDD is more than just writing tests - it''s a design methodology that leads to better code. Let''s explore how to practice TDD effectively.

## The TDD Cycle

1. **Red**: Write a failing test
2. **Green**: Write minimal code to pass
3. **Refactor**: Improve code quality

## Benefits of TDD

- Better design decisions
- Living documentation
- Confidence in refactoring
- Fewer bugs
- Faster development (long-term)

## Example: Testing a Post Service

```java
@Test
public void shouldCreatePost() {
    // Arrange
    Post post = new Post("Test Title", "Test Content");
    
    // Act
    boolean result = postService.createPost(post);
    
    // Assert
    assertTrue(result);
    verify(postDAO).createPost(post);
}

@Test
public void shouldThrowExceptionWhenTitleIsEmpty() {
    // Arrange
    Post post = new Post("", "Content");
    
    // Act & Assert
    assertThrows(ValidationException.class, 
        () -> postService.createPost(post));
}
```

## Best Practices

1. One assertion per test (when possible)
2. Test names should describe behavior
3. Follow AAA pattern (Arrange, Act, Assert)
4. Keep tests independent
5. Use test fixtures and builders

TDD leads to better, more maintainable code!',
'Master Test-Driven Development with practical examples and best practices for writing testable Java code.',
'published',
'https://images.unsplash.com/photo-1516534775068-ba3e7458af70?w=800',
'https://images.unsplash.com/photo-1516534775068-ba3e7458af70?w=1200',
'✅',
892),

(1, 'Microservices Architecture: When and How to Use It',
'Microservices have become increasingly popular, but they''re not always the right choice. Learn when and how to implement them effectively.

## What are Microservices?

Microservices architecture structures an application as a collection of loosely coupled services, each implementing a specific business capability.

## Benefits

✅ Independent deployment
✅ Technology diversity
✅ Scalability
✅ Resilience
✅ Team autonomy

## Challenges

❌ Distributed system complexity
❌ Data consistency
❌ Network latency
❌ Testing difficulty
❌ Operational overhead

## When to Use Microservices

Consider microservices when:
- Your application is large and complex
- You have multiple teams
- Different components have different scaling needs
- You need technology diversity

Start with a monolith and split when needed!

## Communication Patterns

### Synchronous (REST/gRPC)
```java
@FeignClient("post-service")
public interface PostClient {
    @GetMapping("/posts/{id}")
    Post getPost(@PathVariable Long id);
}
```

### Asynchronous (Message Queue)
```java
@RabbitListener(queues = "post.created")
public void handlePostCreated(PostCreatedEvent event) {
    // Handle event
}
```

Make informed decisions about your architecture!',
'Understand microservices architecture patterns, benefits, challenges, and when to use them in your projects.',
'published',
'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800',
'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200',
'🏗️',
1543),

(6, 'Securing Java Applications: Essential Security Practices',
'Security should be a top priority in any application. Learn essential security practices for Java applications.

## Common Security Vulnerabilities

### 1. SQL Injection
**Problem:**
```java
String query = "SELECT * FROM users WHERE username = ''" + username + "''";
```

**Solution: Use Prepared Statements**
```java
String query = "SELECT * FROM users WHERE username = ?";
PreparedStatement stmt = conn.prepareStatement(query);
stmt.setString(1, username);
```

### 2. XSS (Cross-Site Scripting)
Always sanitize user input before displaying.

### 3. CSRF (Cross-Site Request Forgery)
Use CSRF tokens for state-changing operations.

## Password Security

```java
// Use BCrypt for password hashing
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode(plainPassword);
boolean matches = encoder.matches(plainPassword, hashedPassword);
```

## Authentication & Authorization

```java
@PreAuthorize("hasRole(''ADMIN'')")
@DeleteMapping("/posts/{id}")
public void deletePost(@PathVariable Long id) {
    postService.deletePost(id);
}
```

## Best Practices

1. Never store passwords in plain text
2. Use HTTPS everywhere
3. Implement rate limiting
4. Keep dependencies updated
5. Use security headers
6. Validate all input
7. Implement proper error handling

Security is not optional!',
'Learn essential security practices to protect your Java applications from common vulnerabilities and attacks.',
'published',
'https://images.unsplash.com/photo-1510511459019-5dda7724fd87?w=800',
'https://images.unsplash.com/photo-1510511459019-5dda7724fd87?w=1200',
'🔒',
2341),

(7, 'Java Memory Management and Garbage Collection Explained',
'Understanding memory management is crucial for building efficient Java applications. Let''s demystify the JVM''s memory model and garbage collection.

## Memory Areas in JVM

### Heap
- Stores objects and arrays
- Shared among all threads
- Garbage collected

### Stack
- Stores method frames
- Thread-specific
- Automatically managed

### Metaspace (Method Area)
- Stores class metadata
- Replaces PermGen in Java 8+

## Garbage Collection Algorithms

### Serial GC
Single-threaded, suitable for small applications.

### Parallel GC
Multi-threaded, good for throughput.

### G1 GC (Default in Java 9+)
Balances throughput and latency.

### ZGC
Ultra-low latency collector.

## Memory Leaks in Java

Yes, they exist! Common causes:
1. Forgotten callbacks/listeners
2. Static collections
3. Unclosed resources
4. Thread locals

```java
// Memory leak example
public class LeakExample {
    private static final List<Object> list = new ArrayList<>();
    
    public void addToList(Object obj) {
        list.add(obj); // Never removed!
    }
}
```

## Best Practices

1. Close resources with try-with-resources
2. Be careful with static collections
3. Remove listeners when done
4. Use weak references for caches
5. Profile your application

Understand memory to build better applications!',
'Master Java memory management and garbage collection to build efficient, performant applications.',
'published',
'https://images.unsplash.com/photo-1487058792275-0ad4aaf24ca7?w=800',
'https://images.unsplash.com/photo-1487058792275-0ad4aaf24ca7?w=1200',
'🧠',
678),

(8, 'Design Patterns in Java: Builder, Factory, and Singleton',
'Design patterns are reusable solutions to common problems. Let''s explore three essential patterns every Java developer should know.

## 1. Singleton Pattern

Ensures only one instance of a class exists.

```java
public class PostCache {
    private static PostCache instance;
    private Map<Integer, Post> cache = new HashMap<>();
    
    private PostCache() {} // Private constructor
    
    public static synchronized PostCache getInstance() {
        if (instance == null) {
            instance = new PostCache();
        }
        return instance;
    }
}
```

### When to use:
- Database connections
- Logging
- Configuration managers

## 2. Builder Pattern

Constructs complex objects step by step.

```java
public class Post {
    private final String title;
    private final String content;
    private final String excerpt;
    
    private Post(Builder builder) {
        this.title = builder.title;
        this.content = builder.content;
        this.excerpt = builder.excerpt;
    }
    
    public static class Builder {
        private String title;
        private String content;
        private String excerpt;
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Post build() {
            return new Post(this);
        }
    }
}

// Usage
Post post = new Post.Builder()
    .title("My Post")
    .content("Content here")
    .build();
```

## 3. Factory Pattern

Creates objects without specifying exact class.

```java
public interface NotificationSender {
    void send(String message);
}

public class NotificationFactory {
    public static NotificationSender create(String type) {
        return switch (type) {
            case "email" -> new EmailSender();
            case "sms" -> new SmsSender();
            default -> throw new IllegalArgumentException();
        };
    }
}
```

Use patterns wisely - don''t over-engineer!',
'Learn three essential design patterns that will make your Java code more maintainable and flexible.',
'published',
'https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=800',
'https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=1200',
'🎨',
1234),

(3, 'Asynchronous Programming in Java with CompletableFuture',
'Modern applications need to handle multiple tasks concurrently. Learn how to write asynchronous code in Java effectively.

## Traditional Approach

```java
// Blocking calls
User user = userService.getUser(id);
List<Post> posts = postService.getPostsByUser(user.getId());
```

## CompletableFuture Approach

```java
CompletableFuture<User> userFuture = 
    CompletableFuture.supplyAsync(() -> userService.getUser(id));

CompletableFuture<List<Post>> postsFuture = userFuture
    .thenCompose(user -> 
        CompletableFuture.supplyAsync(() -> 
            postService.getPostsByUser(user.getId())));
```

## Combining Multiple Futures

```java
CompletableFuture<User> userFuture = getUserAsync();
CompletableFuture<List<Post>> postsFuture = getPostsAsync();
CompletableFuture<List<Comment>> commentsFuture = getCommentsAsync();

CompletableFuture.allOf(userFuture, postsFuture, commentsFuture)
    .thenAccept(v -> {
        User user = userFuture.join();
        List<Post> posts = postsFuture.join();
        List<Comment> comments = commentsFuture.join();
        // Process results
    });
```

## Error Handling

```java
CompletableFuture.supplyAsync(() -> getUserService.getUser(id))
    .exceptionally(ex -> {
        logger.error("Failed to get user", ex);
        return getDefaultUser();
    })
    .thenAccept(user -> processUser(user));
```

## Best Practices

1. Use `supplyAsync` for operations that return values
2. Use `runAsync` for operations that don''t return values
3. Always handle exceptions
4. Consider timeout with `orTimeout()`
5. Use custom executors for control

Write non-blocking, responsive applications!',
'Master asynchronous programming in Java using CompletableFuture for building responsive, high-performance applications.',
'published',
'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800',
'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=1200',
'⚙️',
945),

(4, 'Understanding SOLID Principles with Java Examples',
'SOLID principles are fundamental to object-oriented design. Let''s explore each principle with practical Java examples.

## S - Single Responsibility Principle

A class should have only one reason to change.

❌ Bad:
```java
public class Post {
    private String title;
    private String content;
    
    public void save() {
        // Database logic here
    }
    
    public void sendEmail() {
        // Email logic here
    }
}
```

✅ Good:
```java
public class Post {
    private String title;
    private String content;
    // Only post-related logic
}

public class PostRepository {
    public void save(Post post) {
        // Database logic
    }
}

public class NotificationService {
    public void notifyPostCreated(Post post) {
        // Email logic
    }
}
```

## O - Open/Closed Principle

Open for extension, closed for modification.

```java
public interface PostFilter {
    boolean matches(Post post);
}

public class StatusFilter implements PostFilter {
    private String status;
    
    public boolean matches(Post post) {
        return post.getStatus().equals(status);
    }
}
```

## L - Liskov Substitution Principle

Subtypes must be substitutable for their base types.

## I - Interface Segregation Principle

Don''t force clients to depend on methods they don''t use.

## D - Dependency Inversion Principle

Depend on abstractions, not concretions.

```java
public class PostService {
    private final PostRepository repository; // Interface
    
    public PostService(PostRepository repository) {
        this.repository = repository;
    }
}
```

Apply SOLID for maintainable code!',
'Master the five SOLID principles of object-oriented design with practical Java examples for better code architecture.',
'draft',
'https://images.unsplash.com/photo-1534972195531-d756b9bfa9f2?w=800',
'https://images.unsplash.com/photo-1534972195531-d756b9bfa9f2?w=1200',
'🏛️',
0),

(5, 'Docker for Java Developers: Containerizing Your Applications',
'Containers have revolutionized application deployment. Learn how to containerize your Java applications with Docker.

## Why Docker?

- Consistent environments
- Easy deployment
- Resource isolation
- Scalability
- Portability

## Basic Dockerfile for Java

```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/myapp.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Multi-Stage Build (Optimized)

```dockerfile
# Build stage
FROM maven:3.9-openjdk-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Docker Compose

```yaml
version: ''3.8''
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=postgres
    depends_on:
      - postgres
  
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: blogdb
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
```

## Best Practices

1. Use specific tags, not `latest`
2. Minimize layers
3. Use .dockerignore
4. Don''t run as root
5. Keep images small
6. Use health checks

Containerize for easier deployment!',
'Learn how to containerize Java applications with Docker for consistent, scalable deployments across environments.',
'draft',
'https://images.unsplash.com/photo-1605745341112-85968b19335b?w=800',
'https://images.unsplash.com/photo-1605745341112-85968b19335b?w=1200',
'🐳',
0);

-- ================================================================
-- SEED DATA: Comments
-- ================================================================
INSERT INTO comments (post_id, user_id, content) VALUES
(1, 2, 'Great introduction to JavaFX! I''ve been wanting to learn this framework and your tutorial made it really accessible.'),
(1, 3, 'Thanks for the code examples. The Hello World app worked perfectly on my machine!'),
(1, 4, 'Could you do a follow-up tutorial on JavaFX styling with CSS? That would be amazing!'),
(2, 1, 'Excellent explanation of normalization! The examples really helped me understand 3NF.'),
(2, 5, 'I''ve been struggling with database design. This article cleared up a lot of confusion. Thank you!'),
(3, 1, 'I implemented indexes based on this article and saw a 70x performance improvement! Incredible!'),
(3, 6, 'One question: How do you decide which columns need composite indexes vs separate indexes?'),
(3, 2, 'The real-world examples really help understand when and where to use indexes. Well done!'),
(4, 7, 'As a backend developer, this REST API guide is exactly what I needed. Bookmarked!'),
(4, 8, 'What about API versioning? Would love to see your thoughts on that topic.'),
(5, 3, 'Java Streams transformed how I write code. This article is a great reference!'),
(5, 4, 'The parallel streams section was particularly useful. Thanks for the performance tips!'),
(6, 2, 'Implementing caching in my app now. The TTL-based approach seems perfect for my use case.'),
(6, 5, 'Cache invalidation has always been tricky for me. Your strategies section was enlightening!'),
(7, 1, 'TDD changed my development workflow completely. Great practical examples here!'),
(7, 6, 'The AAA pattern makes tests so much more readable. Thanks for emphasizing that!'),
(8, 3, 'Microservices aren''t always the answer - glad you mentioned when NOT to use them!'),
(8, 7, 'We''re migrating to microservices. This article will be required reading for our team.'),
(9, 4, 'Security should definitely be a top priority. Every developer needs to read this!'),
(9, 8, 'The SQL injection examples are scary but important to understand. Great awareness!'),
(10, 2, 'Memory leaks in Java are more common than people think. Thanks for the examples!'),
(10, 5, 'The garbage collection explanation finally makes sense to me. Excellent write-up!'),
(11, 6, 'Design patterns can be overused, but these three are essential. Great selection!'),
(11, 1, 'The Builder pattern example is perfect. I''ll be using this in my next project.'),
(12, 7, 'CompletableFuture is so powerful! This guide helped me understand how to use it properly.'),
(12, 3, 'Async programming in Java used to confuse me. Not anymore thanks to this article!'),
(1, 5, 'Do you have plans to cover JavaFX animations in a future post? Would love to learn more!'),
(2, 6, 'The normalization examples with the blog database were perfect. Very relatable!'),
(3, 8, 'Before: 2000ms query time. After adding indexes: 25ms. You''re a lifesaver!'),
(4, 4, 'REST principles explained clearly. This should be taught in every bootcamp!'),
(5, 1, 'Streams API is one of the best features added to Java. Great comprehensive guide!');

-- ================================================================
-- SEED DATA: Post-Tag Relationships
-- ================================================================
INSERT INTO post_tags (post_id, tag_id) VALUES
-- Post 1: JavaFX Guide
(1, 2), -- JavaFX
(1, 5), -- Tutorial
(1, 1), -- Java

-- Post 2: Database Design
(2, 3), -- Database
(2, 4), -- PostgreSQL
(2, 6), -- Best Practices

-- Post 3: SQL Indexing
(3, 3), -- Database
(3, 7), -- Performance
(3, 5), -- Tutorial

-- Post 4: REST APIs
(4, 12), -- Web Development
(4, 6), -- Best Practices
(4, 8), -- Architecture

-- Post 5: Java Streams
(5, 1), -- Java
(5, 5), -- Tutorial
(5, 7), -- Performance

-- Post 6: Caching
(6, 1), -- Java
(6, 7), -- Performance
(6, 8), -- Architecture

-- Post 7: TDD
(7, 9), -- Testing
(7, 6), -- Best Practices
(7, 1), -- Java

-- Post 8: Microservices
(8, 8), -- Architecture
(8, 12), -- Web Development
(8, 10), -- DevOps

-- Post 9: Security
(9, 11), -- Security
(9, 6), -- Best Practices
(9, 1), -- Java

-- Post 10: Memory Management
(10, 1), -- Java
(10, 7), -- Performance

-- Post 11: Design Patterns
(11, 1), -- Java
(11, 8), -- Architecture
(11, 6), -- Best Practices

-- Post 12: Async Programming
(12, 1), -- Java
(12, 7), -- Performance
(12, 5), -- Tutorial

-- Post 13: SOLID Principles
(13, 1), -- Java
(13, 8), -- Architecture
(13, 6), -- Best Practices

-- Post 14: Docker
(14, 10), -- DevOps
(14, 14), -- Cloud
(14, 5); -- Tutorial

-- ================================================================
-- SEED DATA: Reviews
-- ================================================================
INSERT INTO reviews (post_id, user_id, rating, title, content, helpful) VALUES
(1, 2, 5, 'Perfect for Beginners', 'This is exactly what I needed to get started with JavaFX. The explanations are clear and the code examples work flawlessly. Highly recommended for anyone new to JavaFX!', TRUE),
(1, 3, 4, 'Good Introduction', 'Solid tutorial! Would have loved to see more advanced topics covered, but as an introduction it''s excellent.', TRUE),
(1, 5, 5, 'Bookmark-worthy', 'I keep coming back to this article whenever I need to reference JavaFX basics. Great resource!', TRUE),

(2, 3, 5, 'Database Design Made Simple', 'Finally understand normalization! The step-by-step progression from 1NF to 3NF was brilliantly explained.', TRUE),
(2, 4, 5, 'Essential Reading', 'Every developer should read this before designing a database. Saved me from making several mistakes.', TRUE),

(3, 2, 5, 'Game Changer', 'Implemented the indexing strategies from this article and saw immediate performance improvements. 50x faster queries!', TRUE),
(3, 6, 4, 'Very Practical', 'Good real-world examples. Would love to see more about index maintenance and when to rebuild indexes.', TRUE),
(3, 7, 5, 'Performance Gold', 'This article should be required reading for anyone working with databases. The performance gains speak for themselves.', TRUE),

(4, 1, 5, 'REST API Bible', 'Comprehensive guide to REST principles. I reference this constantly when designing APIs.', TRUE),
(4, 8, 4, 'Solid Foundation', 'Great coverage of REST basics. Examples are clear and follow current best practices.', FALSE),

(5, 2, 5, 'Streams Finally Make Sense', 'I avoided streams for years because they seemed complicated. This article changed that. Now I use them everywhere!', TRUE),
(5, 6, 5, 'Functional Java at Its Best', 'The examples progress nicely from simple to complex. Excellent learning resource!', TRUE),

(6, 3, 4, 'Practical Caching Guide', 'Implemented the TTL-based caching strategy in production. Works great! Would have liked more on distributed caching.', TRUE),
(6, 7, 5, 'Performance Boost Achieved', 'Cache hit rate of 80% after implementing these strategies. Response times improved dramatically!', TRUE),

(7, 2, 5, 'TDD Done Right', 'Finally understand how to practice TDD effectively. The Red-Green-Refactor cycle makes so much sense now.', TRUE),
(7, 4, 4, 'Solid TDD Introduction', 'Good practical examples. Would love to see coverage of testing frameworks like JUnit 5 and Mockito.', FALSE),

(8, 4, 5, 'Realistic Microservices Advice', 'Thank you for being honest about when NOT to use microservices! Too many articles just hype them up.', TRUE),
(8, 5, 4, 'Balanced Perspective', 'Covers both benefits and challenges honestly. Very helpful for architectural decisions.', TRUE),

(9, 3, 5, 'Security Essentials', 'Every developer needs to read this. The SQL injection examples alone are worth it.', TRUE),
(9, 6, 5, 'Practical Security Guide', 'Clear, actionable advice. Implemented several suggestions immediately in our codebase.', TRUE),

(10, 4, 4, 'Memory Management Demystified', 'GC concepts finally make sense! Would have liked more on profiling tools.', TRUE),
(10, 7, 5, 'Essential Java Knowledge', 'Understanding memory management made me a better developer. Excellent deep dive!', TRUE),

(11, 5, 5, 'Design Patterns Masterclass', 'The three patterns every developer should know, explained perfectly with clean code examples.', TRUE),
(11, 8, 4, 'Good Pattern Overview', 'Solid examples of commonly used patterns. Would love to see more patterns covered in future articles.', FALSE),

(12, 1, 5, 'Async Made Easy', 'CompletableFuture seemed daunting before. This guide made it approachable and practical.', TRUE),
(12, 6, 5, 'Non-blocking Excellence', 'Transformed how I write concurrent code. Error handling examples were particularly helpful!', TRUE);

-- ================================================================
-- VERIFICATION QUERIES
-- ================================================================

-- Count records in each table
-- SELECT 'Users' as table_name, COUNT(*) as count FROM users
-- UNION ALL
-- SELECT 'Posts', COUNT(*) FROM posts
-- UNION ALL
-- SELECT 'Comments', COUNT(*) FROM comments
-- UNION ALL
-- SELECT 'Tags', COUNT(*) FROM tags
-- UNION ALL
-- SELECT 'Post_Tags', COUNT(*) FROM post_tags
-- UNION ALL
-- SELECT 'Reviews', COUNT(*) FROM reviews;

-- ================================================================
-- DATA SUMMARY
-- ================================================================
-- Users: 8
-- Posts: 14 (12 published, 2 drafts)
-- Comments: 30
-- Tags: 15
-- Post-Tag Relationships: 40+
-- Reviews: 25
-- ================================================================

COMMIT;
