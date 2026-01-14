# HTTP API Testing Guide

This directory contains comprehensive HTTP test files for the Blog Backend API. These files can be used with REST Client extensions in VS Code, IntelliJ IDEA HTTP Client, or similar tools.

## 📁 Test Files

### 1. **auth.http**
Authentication and authorization tests:
- User registration
- User login
- Token validation
- Password reset

### 2. **posts.http**
Post management tests:
- Create, read, update, delete posts
- Pagination and sorting
- Search functionality
- Filter by category/user
- Validation tests

### 3. **comments.http**
Comment management tests:
- Create, update, delete comments
- Nested comments (replies)
- Pagination
- Comment approval/rejection
- Authorization tests

### 4. **categories.http**
Category management tests:
- CRUD operations
- Slug validation
- Duplicate prevention
- Popular categories

### 5. **users.http**
User profile tests:
- Get user details
- Update profile
- Change password
- Avatar management
- User statistics

### 6. **graphql.http**
GraphQL API tests:
- Queries (posts, comments, users, categories)
- Mutations (create, update, delete)
- Pagination
- Complex nested queries
- Error handling

### 7. **validation.http**
Validation and error handling tests:
- Missing required fields
- Invalid data types
- Constraint violations
- SQL injection prevention
- XSS prevention
- Authentication errors
- Authorization errors

## 🚀 Getting Started

### Prerequisites
1. Start the database:
   ```bash
   ./dev.sh start
   ```

2. Run the Spring Boot application:
   ```bash
   mvn clean javafx:run
   # Or for backend only:
   cd blog-backend && mvn spring-boot:run
   ```

### Using with VS Code
1. Install the "REST Client" extension
2. Open any `.http` file
3. Click "Send Request" above any request
4. View results in the response panel

### Using with IntelliJ IDEA
1. HTTP Client is built-in
2. Open any `.http` file
3. Click the green arrow next to any request
4. View results in the HTTP Response panel

## 📝 Test Execution Order

For initial testing, follow this sequence:

1. **Authentication** (`auth.http`):
   - Register a new user
   - Login to get authentication token
   - Save the token for subsequent requests

2. **Categories** (`categories.http`):
   - Create categories first (posts need categories)
   - Test CRUD operations

3. **Posts** (`posts.http`):
   - Create posts with the user token
   - Test pagination, sorting, filtering
   - Test search functionality

4. **Comments** (`comments.http`):
   - Add comments to posts
   - Test nested comments
   - Test moderation features

5. **GraphQL** (`graphql.http`):
   - Test equivalent GraphQL queries
   - Compare performance with REST

6. **Validation** (`validation.http`):
   - Test error handling
   - Verify security measures

## 🔑 Variables

Most test files use these variables:
- `@host` - API base URL (default: http://localhost:8080/api)
- `@token` - JWT authentication token
- `@graphqlEndpoint` - GraphQL endpoint URL

Update these at the top of each file as needed.

## ✅ Expected Results

### Success Responses
All successful operations return a structured response:
```json
{
  "status": "success",
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Error Responses
Errors return detailed information:
```json
{
  "status": "error",
  "message": "Error description",
  "data": null
}
```

### Validation Errors
Validation failures include field-specific errors:
```json
{
  "status": "fail",
  "message": "Validation failed",
  "data": {
    "field1": "Error message",
    "field2": "Error message"
  }
}
```

## 📊 Testing Features

### Pagination
Most list endpoints support:
- `page` - Page number (0-indexed)
- `size` - Items per page
- `sortBy` - Field to sort by
- `sortDir` - Sort direction (ASC/DESC)

Example:
```
GET /posts?page=0&size=10&sortBy=createdAt&sortDir=DESC
```

### Search and Filtering
Search endpoints typically support:
- `keyword` - Search term
- `status` - Filter by status
- `categoryId` - Filter by category
- `userId` - Filter by user

### GraphQL Queries
GraphQL supports flexible data fetching:
- Request only needed fields
- Nested queries
- Pagination
- Variables for type safety

## 🐛 Troubleshooting

### Common Issues

**1. Connection refused**
- Ensure the backend is running
- Check if port 8080 is available
- Verify database is running

**2. 401 Unauthorized**
- Login to get a fresh token
- Update the `@token` variable
- Check token expiration

**3. 404 Not Found**
- Verify the resource ID exists
- Check the endpoint URL
- Ensure database is seeded

**4. Validation errors**
- Review request payload format
- Check required fields
- Verify data types

## 📈 Performance Testing

Compare REST vs GraphQL performance:

1. Run equivalent REST and GraphQL queries
2. Note response times in test results
3. Compare payload sizes
4. Analyze N+1 query issues

Example comparison:
- REST: Multiple requests for related data
- GraphQL: Single request with nested data

## 🔒 Security Testing

The validation.http file includes tests for:
- SQL injection prevention
- XSS attack prevention
- Authentication bypass attempts
- Authorization checks
- Input validation
- Rate limiting (if implemented)

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [GraphQL Documentation](https://graphql.org/)
- [REST Client Extension](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)
- [OpenAPI/Swagger UI](http://localhost:8080/api/swagger-ui.html)
- [GraphiQL Interface](http://localhost:8080/api/graphiql)

## 🤝 Contributing

When adding new endpoints:
1. Create corresponding HTTP tests
2. Include positive and negative test cases
3. Add validation tests
4. Document expected responses
5. Update this README
