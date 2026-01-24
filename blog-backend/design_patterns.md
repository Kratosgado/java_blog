# Design Patterns Used in the Project

## 1. Repository Pattern
- **Usage:** Used in `PostRepository`, `CategoryRepository`, `TagRepository`.
- **Description:** Abstracts data access logic and provides a collection-like interface for accessing domain objects. It decouples the business logic from the data access layer.

## 2. Service Layer Pattern
- **Usage:** Implemented in `PostService`, `CategoryService`, `TagService`, `AuthService`, etc.
- **Description:** Encapsulates the application's business logic, acting as a boundary between the controller (presentation layer) and the repository (data access layer).

## 3. Data Transfer Object (DTO) Pattern
- **Usage:** Used extensively with request and response objects (e.g., `PostResponse`, `CreatePostRequest`).
- **Description:** Carries data between processes to reduce the number of method calls and to decouple the internal domain model from the external API contract.

## 4. Builder Pattern
- **Usage:** Used in `Category` entity (via Lombok `@Builder`) and likely other entities.
- **Description:** Provides a flexible solution for constructing complex objects. It separates the construction of a complex object from its representation.

## 5. Singleton Pattern
- **Usage:** Spring Beans (e.g., Services, Repositories, Components) are singletons by default.
- **Description:** Ensures a class has only one instance and provides a global point of access to it.

## 6. Aspect-Oriented Programming (AOP) (Proxy Pattern)
- **Usage:** Evident in `LoggingAspect`, `PerformanceAspect`, `CacheAspect`, and `@Transactional` annotations.
- **Description:** Allows separation of cross-cutting concerns (logging, transaction management, caching) from the main business logic. Spring uses dynamic proxies to implement this.

## 7. Adapter Pattern
- **Usage:** `DtoMapper` class.
- **Description:** Converts the interface of a class (Entity) into another interface (DTO) that the client expects.

## 8. Strategy Pattern
- **Usage:** Implicitly used in Spring Security (e.g., authentication providers) and Spring Data JPA (dialect resolution).
- **Description:** Defines a family of algorithms, encapsulates each one, and makes them interchangeable.

## 9. Dependency Injection (Inversion of Control)
- **Usage:** Throughout the application (e.g., `@Autowired`, constructor injection in Services and Controllers).
- **Description:** Decouples components by injecting dependencies rather than having components create them.

## 10. Facade Pattern
- **Usage:** Controllers (e.g., `PostController`) act as a facade.
- **Description:** Provides a simplified interface to a larger body of code (the service layer), handling HTTP requests and delegating to services.
