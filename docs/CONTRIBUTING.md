# Contributing Guide

Thank you for your interest in contributing to the Smart Blogging Platform! This guide provides guidelines and instructions for contributing.

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Report issues responsibly
- Follow project coding standards

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Git
- IDE (IntelliJ IDEA, Eclipse, or VS Code recommended)

### Fork and Clone

```bash
# Fork the repository on GitHub
# Clone your fork
git clone https://github.com/YOUR_USERNAME/blog.git
cd blog

# Add upstream remote
git remote add upstream https://github.com/kratosgado/blog.git
```

### Set Up Development Environment

```bash
# Install dependencies
mvn clean install

# Start PostgreSQL
./dev.sh start

# Start MongoDB
docker run -d --name mongodb -p 27017:27017 mongo:6.0

# Run tests
mvn test

# Start backend for local development
mvn -pl blog-backend spring-boot:run
```

## Development Workflow

### 1. Create a Feature Branch

```bash
# Update main branch
git fetch upstream
git checkout main
git merge upstream/main

# Create feature branch
git checkout -b feat/your-feature-name

# Or for bug fixes
git checkout -b fix/bug-name

# Or for documentation
git checkout -b docs/update-name
```

### 2. Implement Changes

**Code Style Guidelines**:

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Add JavaDoc for public APIs
- Keep methods focused and concise
- Maximum line length: 120 characters

**Project Structure**:

```
blog-backend/
├── controllers/          # REST endpoints
├── services/             # Business logic
├── repositories/         # Data access layer
├── models/              # Domain entities (in blog-common)
├── dtos/                # Request/response objects
├── config/              # Spring configuration
├── security/            # Security components
├── aspects/             # AOP aspects
└── exceptions/          # Custom exceptions
```

### 3. Write Tests

**Test Requirements**:

- Unit tests for services
- Integration tests for repositories
- Controller tests with MockMvc
- Test coverage minimum: 70% for new code

**Test Structure**:

```java
public class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldCreatePost() {
        // Arrange
        CreatePostRequest request = new CreatePostRequest(...);

        // Act
        Post result = postService.createPost(request);

        // Assert
        assertNotNull(result);
        assertEquals("title", result.getTitle());
    }
}
```

**Run Tests**:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PostServiceTest

# Run specific test method
mvn test -Dtest=PostServiceTest#shouldCreatePost

# Run with coverage
mvn test jacoco:report
```

### 4. Commit Changes

**Commit Message Format**:

```
type(scope): subject

body

footer
```

**Type Options**:

- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation
- `style` - Code style (no logic change)
- `refactor` - Code refactoring
- `perf` - Performance improvement
- `test` - Test additions/modifications
- `chore` - Build/dependency changes

**Example**:

```bash
git add .
git commit -m "feat(post): add draft post functionality

- Add draft post creation endpoint
- Implement draft status in Post model
- Add validation for draft posts

Closes #123"
```

### 5. Push and Create Pull Request

```bash
# Push to your fork
git push origin feat/your-feature-name

# Create pull request on GitHub
# - Provide clear description
# - Reference related issues
# - Add screenshots for UI changes
```

## Pull Request Guidelines

### PR Title

Clear and descriptive:

- ✅ "Add full-text search for posts"
- ✅ "Fix N+1 query problem in PostService"
- ❌ "Update code"
- ❌ "Bug fix"

### PR Description

```markdown
## Description

Brief description of changes

## Related Issues

Closes #123

## Type of Change

- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing

How to test the changes:

1. Start application
2. Navigate to...
3. Verify...

## Checklist

- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No breaking changes
- [ ] Follows code style
```

### PR Review Process

1. **Automated Checks**:
   - Tests must pass
   - Build must succeed
   - Code coverage maintained
   - No merge conflicts

2. **Code Review**:
   - Maintainers review changes
   - Feedback provided as comments
   - Author addresses feedback
   - Re-review after updates

3. **Approval**:
   - At least 2 approvals required
   - All conversations resolved
   - Tests passing
   - Ready to merge

## Adding New Features

### Example: Adding a New Endpoint

**Step 1: Create Request/Response DTOs**

```java
// blog-common/dtos/request/CreateCommentRequest.java
public record CreateCommentRequest(
    @NotNull Long postId,
    @NotBlank String content
) {}

// blog-common/dtos/response/CommentResponse.java
public interface CommentResponse {
    Long getId();
    String getContent();
    LocalDateTime getCreatedAt();
    UserView getAuthor();

    interface UserView {
        Long getId();
        String getUsername();
    }
}
```

**Step 2: Implement Service Layer**

```java
@Service
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;

    @Transactional
    @CacheEvict(value = "COMMENTLIST", key = "#request.postId")
    public CommentResponse createComment(CreateCommentRequest request, User author) {
        // Validation
        Post post = postService.getPostById(request.postId());

        // Create entity
        Comment comment = new Comment(request.content(), post, author);

        // Save
        return commentRepository.save(comment);
    }
}
```

**Step 3: Implement Repository**

```java
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
        SELECT c FROM Comment c
        JOIN FETCH c.author
        WHERE c.post.id = :postId
        ORDER BY c.createdAt DESC
    """)
    Page<CommentResponse> findByPostId(@Param("postId") Long postId, Pageable pageable);
}
```

**Step 4: Implement Controller**

```java
@RestController
@RequestMapping("/comments")
@Tag(name = "Comments", description = "Comment management")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
   @SecuredCreateEndpoint(
      summary = "Create a comment",
      description = "Creates a new comment on a post. Requires authentication.",
      roles = {UserRole.AUTHOR, UserRole.ADMIN})
    public CommentResponse createComment(
        @Valid @RequestBody CreateCommentRequest request,
        @AuthenticationPrincipal User user) {
        return commentService.createComment(request, user);
    }

    @GetMapping("/post/{postId}")
    @GetEndpoint(
      summary = "Get comments for a post",
      description = "Retrieves all approved comments for a specific post. Public access.")
    public Page<CommentResponse> getComments(
        @PathVariable Long postId,
        @ParameterObject Pageable pageable) {
        return commentService.getCommentsByPostId(postId, pageable);
    }
}
```

**Step 5: Add Tests**

```java
@SpringBootTest
public class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    public void shouldCreateComment() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest(1L, "Great post!");

        mockMvc.perform(post("/api/v1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}
```

## Performance Optimization Guidelines

When implementing features:

1. **Use Caching**: Mark frequently accessed data with `@Cacheable`
2. **Optimize Queries**: Use `@EntityGraph` to prevent N+1
3. **Add Indexes**: Request database indexes for new searchable fields
4. **Pagination**: Always paginate list endpoints
5. **Monitor**: Check query performance with EXPLAIN ANALYZE

## Database Changes

### Adding a New Column

```sql
-- Migration script
ALTER TABLE posts ADD COLUMN featured_image_url VARCHAR(500);

-- Add index if searchable
CREATE INDEX idx_posts_featured_image ON posts(featured_image_url);
```

### Adding a New Table

```sql
-- Create table
CREATE TABLE bookmarks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, post_id)
);

-- Add indexes
CREATE INDEX idx_bookmarks_user_id ON bookmarks(user_id);
CREATE INDEX idx_bookmarks_post_id ON bookmarks(post_id);
```

## Security Considerations

- Never hardcode secrets or API keys
- Validate all user input on server-side
- Use parameterized queries (prevention of SQL injection)
- Check authorization on sensitive operations
- Follow [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- Sanitize output to prevent XSS
- Use HTTPS in production
- Hash passwords with bcrypt

## Documentation

### JavaDoc

```java
/**
 * Creates a new blog post with the provided details.
 *
 * @param request the post creation request containing title, content, etc.
 * @param author the authenticated user creating the post
 * @return the created post with assigned ID
 * @throws ValidationException if request data is invalid
 * @throws ForbiddenException if user lacks AUTHOR role
 */
public Post createPost(CreatePostRequest request, User author) {
    // implementation
}
```

### README Updates

Update relevant documentation:

- API endpoints documentation
- Feature descriptions
- Configuration options
- Installation steps (if applicable)

## Issues and Discussions

### Reporting Issues

**Create an issue** for:

- Bug reports
- Feature requests
- Documentation improvements
- Performance concerns

**Issue Template**:

```markdown
## Description

What is the issue?

## Steps to Reproduce

1. First step
2. Second step
3. Expected vs actual result

## Environment

- Java version
- OS
- Browser (if applicable)

## Screenshots

If applicable, add screenshots
```

### Discussion Guidelines

- Ask questions in GitHub Discussions
- Suggest improvements before implementing
- Discuss architecture changes before coding
- Collaborate on complex features

## Build and Test Checklist

Before submitting PR:

```bash
# Clean build
mvn clean install

# Run all tests
mvn test

# Run integration tests
mvn verify

# Check code style
mvn checkstyle:check

# Generate javadoc
mvn javadoc:javadoc

# Run specific test
mvn test -Dtest=PostServiceTest

# Package application
mvn clean package
```

## Release Process

1. Bump version number (MAJOR.MINOR.PATCH)
2. Update CHANGELOG
3. Tag release on GitHub
4. Create release notes
5. Deploy to production

## Getting Help

- **Questions**: Use GitHub Discussions
- **Issues**: Create GitHub Issue
- **Security**: Email <security@example.com> (do not create public issue)
- **Chat**: Slack/Discord (if available)

## Maintainers

- Primary: @kratosgado
- Reviewers: Team members with write access

## License

All contributions are licensed under the MIT License. By contributing, you agree that your contributions will be licensed under its MIT License.

---

## Contribution Ideas

### Good for First-Time Contributors

- [ ] Add unit tests for existing code
- [ ] Improve documentation
- [ ] Fix typos and grammar
- [ ] Add API endpoint examples
- [ ] Create helper utility functions

### Intermediate Contributions

- [ ] Add new features from issues
- [ ] Performance optimizations
- [ ] Database migration support
- [ ] Error handling improvements
- [ ] Caching enhancements

### Advanced Contributions

- [ ] Architecture improvements
- [ ] Multi-database support
- [ ] Advanced search features
- [ ] Real-time features (WebSockets)
- [ ] Mobile app integration

---

Thank you for contributing to the Smart Blogging Platform! 🚀

## Related Documentation

- [Installation Guide](INSTALLATION.md) - Setup development environment
- [Architecture Overview](ARCHITECTURE.md) - System design
- [API Endpoints](ENDPOINTS.md) - API reference
- [Security Guide](SECURITY.md) - Security best practices
