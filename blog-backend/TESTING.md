# Blog Backend - Test Suite Documentation

This document provides comprehensive information about the test suite for the Blog Backend application.

## Test Coverage Overview

The test suite covers the following components:

### 1. Service Layer Tests (Unit Tests)
- **AuthServiceTest** - Authentication and user registration
- **PostServiceTest** - Blog post CRUD operations
- **CommentServiceTest** - Comment management and moderation
- **CategoryServiceTest** - Category management with slug generation
- **TagServiceTest** - Tag management with slug generation
- **UserServiceTest** - User profile and account management

### 2. Security Layer Tests
- **JwtUtilTest** - JWT token generation, validation, and claim extraction

### 3. Controller Layer Tests (Unit Tests with MockMvc)
- **AuthControllerTest** - Authentication endpoints

### 4. Exception Handling Tests
- **GlobalExceptionHandlerTest** - Global exception handling for various error scenarios

### 5. Integration Tests
- **AuthIntegrationTest** - End-to-end authentication flow tests

## Test Statistics

### Total Test Files: 10
### Estimated Total Test Cases: 150+

#### Service Tests
- **AuthServiceTest**: 6 test cases
  - Valid login
  - Invalid email/password scenarios
  - User registration
  - Duplicate email handling
  - Default role assignment

- **PostServiceTest**: 11 test cases
  - Create, update, delete posts
  - Get posts by various criteria
  - Search functionality
  - Partial updates
  - Authorization checks

- **CommentServiceTest**: 11 test cases
  - Create comments with pending status
  - Approve/reject comments
  - Delete own comments
  - Authorization checks
  - Comment count aggregation

- **CategoryServiceTest**: 12 test cases
  - Create, update, delete categories
  - Slug generation from names
  - Duplicate detection
  - Special character handling

- **TagServiceTest**: 15 test cases
  - Create, update, delete tags
  - Slug generation
  - Search and pagination
  - Duplicate handling

- **UserServiceTest**: 14 test cases
  - Get users by ID, email, username
  - Update profile with validation
  - Avatar updates
  - Password changes with verification
  - Authorization checks

#### Security Tests
- **JwtUtilTest**: 13 test cases
  - Token generation
  - Username and userId extraction
  - Token validation
  - Custom claims handling
  - Expired/malformed token handling
  - Signature verification

#### Controller Tests
- **AuthControllerTest**: 11 test cases
  - Login endpoint validation
  - Registration endpoint validation
  - Token validation endpoint
  - Request validation
  - Error handling

#### Exception Handling Tests
- **GlobalExceptionHandlerTest**: 7 test cases
  - BlogException handling
  - Validation exceptions
  - IllegalArgument/IllegalState exceptions
  - Access denied scenarios
  - Runtime and generic exceptions

#### Integration Tests
- **AuthIntegrationTest**: 7 test cases
  - Complete auth flow (register → login → validate)
  - Duplicate email prevention
  - Wrong password handling
  - Invalid token validation
  - Request validation

## Running the Tests

### Run all tests
```bash
mvn test
```

### Run specific test class
```bash
mvn test -Dtest=AuthServiceTest
```

### Run tests with coverage
```bash
mvn clean test jacoco:report
```

### Run only unit tests
```bash
mvn test -Dtest=*Test
```

### Run only integration tests
```bash
mvn test -Dtest=*IntegrationTest
```

## Test Configuration

### Test Profile
Tests use the `application-test.properties` configuration with:
- In-memory H2 database (or test PostgreSQL database)
- Test MongoDB instance
- Shortened JWT expiration for faster testing
- Disabled Swagger/GraphQL UI
- Reduced logging levels

### Test Dependencies
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Spring testing utilities
- **MockMvc** - REST API testing
- **AssertJ** - Fluent assertions

## Test Categories

### Unit Tests
Unit tests focus on testing individual components in isolation using mocks:
- All service tests
- JWT utility tests
- Controller tests (with MockMvc)

### Integration Tests
Integration tests verify complete workflows with actual Spring context:
- Authentication flow tests
- Database interactions
- Full request/response cycles

## Key Testing Patterns

### 1. Arrange-Act-Assert (AAA)
All tests follow the AAA pattern:
```java
@Test
void testMethod() {
    // Arrange - Set up test data and mocks
    // Act - Execute the method under test
    // Assert - Verify the results
}
```

### 2. Given-When-Then (in integration tests)
Integration tests describe behavior using Given-When-Then:
- **Given**: Initial state
- **When**: Action performed
- **Then**: Expected outcome

### 3. Test Naming Convention
Tests use descriptive names following the pattern:
`methodName_StateUnderTest_ExpectedBehavior`

Example: `login_WithValidCredentials_ShouldReturnUser`

## Coverage Goals

- **Line Coverage**: > 80%
- **Branch Coverage**: > 75%
- **Method Coverage**: > 85%

## Test Data Management

### Mocking Strategy
- Repository methods are mocked in service tests
- Services are mocked in controller tests
- No mocking in integration tests (uses real beans)

### Test Data Builders
Test data is created using:
- Builder pattern for complex objects (User, Post, etc.)
- Factory methods for simple objects
- Consistent test data across related tests

## Continuous Integration

Tests are automatically run on:
- Every commit (via Git hooks if configured)
- Pull request creation
- Merge to main branch
- Scheduled nightly builds

## Best Practices

1. **Isolation**: Each test is independent and can run in any order
2. **Single Responsibility**: Each test verifies one specific behavior
3. **Meaningful Assertions**: Use descriptive assertion messages
4. **Test Coverage**: Aim for high coverage but focus on critical paths
5. **Performance**: Keep unit tests fast (< 100ms per test)
6. **Maintainability**: Keep tests simple and readable

## Known Limitations

1. GraphQL endpoint tests not yet implemented
2. Aspect (logging/performance) tests not yet implemented
3. File upload functionality tests not included
4. WebSocket tests (if applicable) not included

## Future Improvements

1. Add mutation testing with PIT
2. Add contract testing with Pact
3. Add performance/load testing
4. Increase integration test coverage
5. Add GraphQL query/mutation tests
6. Add end-to-end tests with Selenium/Playwright

## Troubleshooting

### Common Test Failures

**Database connection issues**
- Ensure test database is running
- Check `application-test.properties` configuration

**JWT secret issues**
- Verify JWT secret is properly set in test configuration
- Ensure secret key length meets requirements

**Mocking issues**
- Check Mockito version compatibility
- Verify mock setup in `@BeforeEach` methods

## Contributing

When adding new tests:
1. Follow existing naming conventions
2. Add tests for both happy path and error cases
3. Update this documentation
4. Ensure all tests pass before committing
5. Aim for > 80% code coverage for new features

## Contact

For questions about the test suite, please contact the development team or open an issue.
