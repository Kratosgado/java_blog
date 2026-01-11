# AGENTS.md - Development Guidelines for Java Blog Project

This file contains build commands, code style guidelines, and development conventions for agentic coding agents working in this Java Blog project.

## Build Commands

### Core Maven Commands
- **Build project**: `mvn clean compile`
- **Run application**: `mvn clean javafx:run`
- **Package JAR**: `mvn clean package`
- **Run tests**: `mvn test`
- **Run single test**: `mvn test -Dtest=ClassName#methodName`
- **Run test class**: `mvn test -Dtest=ClassName`
- **Clean build artifacts**: `mvn clean`

### Database Management
- **Start database**: `./dev.sh start`
- **Stop database**: `./dev.sh exit`
- **Database runs on PostgreSQL via Docker container named "postgis"**

### Development Mode
- **Hot reload enabled**: Application includes FXML hot reload in developer mode
- **Main class**: `com.kratosgado.blog.App`

## Code Style Guidelines

### Package Structure
- Base package: `com.kratosgado.blog`
- Follow standard Maven directory structure
- Controllers: `controllers/`
- Services: `services/`
- DAOs: `dao/`
- Models: `models/`
- DTOs: `dtos/request/`
- Utils: `utils/` with subpackages for specific functionality

### Import Organization
1. Java standard library imports (`java.*`)
2. Third-party library imports (org.*, com.*, io.*)
3. Project imports (`com.kratosgado.blog.*`)
4. Blank line between each group
5. No wildcard imports (`import java.util.*;` is forbidden)

### Class and Method Formatting
- **Indentation**: 2 spaces (no tabs)
- **Braces**: K&R style - opening brace on same line
- **Line length**: Maximum 120 characters
- **Class ordering**: Fields → Constructors → Methods
- **Method ordering**: Public → Protected → Private
- **Static methods**: After instance methods

### Naming Conventions
- **Classes**: PascalCase (e.g., `LoginController`, `AuthService`)
- **Methods**: camelCase (e.g., `handleLogin`, `getUserById`)
- **Fields**: camelCase (e.g., `emailField`, `userDAO`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `logger`)
- **Packages**: lowercase with dots
- **Records**: PascalCase with record components in camelCase

### Lombok Usage
- **Models**: Use `@Data` annotation for POJOs
- **No manual getters/setters** when Lombok is available
- **Constructor patterns**: Keep constructors for record-like behavior in models

### Exception Handling
- **Custom exceptions**: Use `BlogExceptions` factory methods:
  - `BlogExceptions.badRequest(message)`
  - `BlogExceptions.notFound(message)`
  - `BlogExceptions.conflict(message)`
  - `BlogExceptions.unauthorized(message)`
  - `BlogExceptions.internal(message)`
- **Logging**: Always log exceptions with context
- **Return types**: Use `Optional<T>` for methods that may not return values

### Validation Pattern
- **DTOs**: Use records with validation annotations
- **Validation**: Call `Validator.validate(dto)` at start of service methods
- **Custom validators**: Located in `utils.validators` package
- **Available annotations**: `@NotNull`, `@IsEmail`, `@IsStrongPassword`, `@Min`, `@IsString`

### Database Pattern
- **DAOs**: Handle all database operations
- **Connection management**: Use try-with-resources for connections
- **SQL**: Use prepared statements to prevent injection
- **Logging**: Log database operations (info for success, error for failures)
- **Return types**: Use `Optional<T>` for queries that may return no results

### JavaFX/Controller Pattern
- **FXML injection**: Use `@FXML` annotation for UI components
- **Initialization**: Use `@FXML private void initialize()` method
- **Navigation**: Use `Navigator.getInstance()` for scene transitions
- **Routes**: Use constants from `Routes` class
- **Authentication**: Use `AuthContext.getInstance()` for user session

### Logging Standards
- **Logger declaration**: `private static final Logger logger = LoggerFactory.getLogger(ClassName.class);`
- **Log levels**: 
  - `logger.info()` for important business operations
  - `logger.debug()` for detailed debugging
  - `logger.error()` for exceptions and errors
- **Context**: Include relevant data in log messages (e.g., user ID, email)

### Service Layer Pattern
- **Business logic**: Encapsulated in service classes
- **Dependency injection**: Constructor injection for DAOs
- **Transaction boundaries**: Service methods should handle complete business operations
- **Validation**: Validate input DTOs before processing

### Testing Guidelines
- **Test location**: `src/test/java/com/kratosgado/blog/`
- **Test naming**: `ClassNameTest` for test classes
- **Method naming**: `methodName_condition_expectedResult`
- **Framework**: Use JUnit (currently minimal test structure exists)

### Security Guidelines
- **Password handling**: Always hash passwords using `ValidationUtils.hashPassword()`
- **SQL injection**: Use prepared statements exclusively
- **Input validation**: Validate all user inputs using the validation framework
- **Authentication**: Use `AuthService` for all authentication operations

### Development Features
- **Hot reload**: FXML files automatically reload in developer mode
- **Developer mode**: Controlled by `DeveloperMode.getInstance()`
- **MaterialFX**: Use MaterialFX components for consistent UI
- **Theme**: Application uses MaterialFX theming with JavaFX Modena

## Common Patterns to Follow

### Controller Structure
```java
public class ExampleController {
  private static final Logger logger = LoggerFactory.getLogger(ExampleController.class);
  
  @FXML private MFXTextField field;
  @FXML private MFXButton button;
  
  private final ServiceClass service;
  
  public ExampleController() {
    this.service = new ServiceClass();
  }
  
  @FXML
  private void initialize() {
    button.setOnAction(e -> handleAction());
  }
  
  private void handleAction() {
    try {
      // Business logic
    } catch (Exception ex) {
      logger.error("Action failed", ex);
      // Show error to user
    }
  }
}
```

### Service Method Structure
```java
public ReturnType methodName(DtoType dto) {
  Validator.validate(dto);
  // Business logic
  // Database operations via DAO
  // Return result or throw exception
}
```

### DAO Method Structure
```java
public Optional<Entity> method(Params params) {
  String sql = "SELECT * FROM table WHERE condition = ?";
  try (Connection conn = DatabaseConfig.getConnection();
       PreparedStatement stmt = conn.prepareStatement(sql)) {
    stmt.setParam(1, param);
    ResultSet rs = stmt.executeQuery();
    if (rs.next()) {
      return Optional.of(new Entity(...));
    }
    return Optional.empty();
  } catch (Exception e) {
    logger.error("Database operation failed", e);
    return Optional.empty();
  }
}
```

## Dependencies and Technologies
- **Java**: 21
- **JavaFX**: 21
- **MaterialFX**: 11.17.0
- **Lombok**: 1.18.42
- **PostgreSQL**: 42.7.8
- **SLF4J**: 2.0.7
- **Gson**: 2.13.2
- **BCrypt**: 0.10.2