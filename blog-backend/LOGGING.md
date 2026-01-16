# Logging Configuration Guide

## Overview
The application uses **Logback** for logging with custom configuration in `logback-spring.xml`.

## Log Locations

### Development Mode
- **Console**: Colored, formatted logs with timestamps
- **File**: `logs/blog-backend.log` - All logs (INFO and above)
- **Error File**: `logs/blog-backend-error.log` - Error logs only

### Production Mode
- **File**: `logs/blog-backend.log` - All logs (INFO and above)
- **Error File**: `logs/blog-backend-error.log` - Error logs only
- **No Console Output** - Reduces overhead

### Test Mode
- **Console Only**: DEBUG level for application code

## Log Format

### Console (Development)
```
2026-01-15 06:53:40.123 INFO  [main] c.k.b.backend.BlogApplication - Application started
```

- Timestamp: `yyyy-MM-dd HH:mm:ss.SSS`
- Level: Color-coded (INFO, WARN, ERROR, DEBUG)
- Thread: `[thread-name]`
- Logger: Abbreviated package name (last 36 chars)
- Message: Log message

### File
```
2026-01-15 06:53:40.123 INFO  [main] com.kratosgado.blog.backend.BlogApplication - Application started
```
Same format without colors for file storage.

## Log Levels by Profile

### Development (`dev`)
- Application (`com.kratosgado.blog`): **INFO**
- Spring Framework: **INFO**
- Hibernate SQL: **DEBUG**
- MongoDB Driver: **INFO**

### Production (`prod`)
- Application: **INFO**
- Spring Framework: **WARN**
- Hibernate: **WARN**
- MongoDB: **WARN**

### Test (`test`)
- Application: **DEBUG**
- Spring Framework: **WARN**
- Hibernate: **WARN**

## Log Rotation

- **Daily Rotation**: Logs rotate at midnight
- **Retention**: 30 days for main logs, 90 days for error logs
- **Size Cap**: 1GB total for main logs
- **Format**: `blog-backend.YYYY-MM-DD.log`

## Changing Log Levels

### Temporarily (Runtime)
You can change log levels without restarting using Spring Boot Actuator:

```bash
# Set specific logger to DEBUG
curl -X POST http://localhost:8080/api/actuator/loggers/com.kratosgado.blog \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# View current log levels
curl http://localhost:8080/api/actuator/loggers/com.kratosgado.blog
```

### Permanently
Edit `blog-backend/src/main/resources/logback-spring.xml`:

```xml
<!-- Change application logger level -->
<logger name="com.kratosgado.blog" level="DEBUG" additivity="false">
  <appender-ref ref="CONSOLE"/>
  <appender-ref ref="FILE"/>
</logger>
```

## Common Logging Patterns in Code

### Controller
```java
private static final Logger logger = LoggerFactory.getLogger(ControllerName.class);

logger.info("Request received: {}", request);
logger.error("Error processing request", exception);
```

### Service
```java
private static final Logger logger = LoggerFactory.getLogger(ServiceName.class);

logger.debug("Processing data: {}", data);
logger.warn("Unusual condition detected: {}", condition);
```

### Performance Logging
```java
long startTime = System.currentTimeMillis();
// ... operation ...
logger.info("Operation completed in {}ms", System.currentTimeMillis() - startTime);
```

## Troubleshooting

### No logs appearing
1. Check if `logback-spring.xml` is in `src/main/resources/`
2. Verify Spring profile is active: `spring.profiles.active=dev`
3. Check log level isn't set too high

### Too many logs
1. Reduce log level for noisy loggers in `logback-spring.xml`
2. Set Spring Framework to WARN: `<logger name="org.springframework" level="WARN"/>`
3. Disable SQL logging: `<logger name="org.hibernate.SQL" level="INFO"/>`

### Logs directory not created
The `logs/` directory is created automatically on first run. Ensure:
- Application has write permissions
- `logs/` is in `.gitignore` (already configured)

## Best Practices

1. **Use appropriate levels**:
   - `ERROR`: Something failed
   - `WARN`: Something unexpected but handled
   - `INFO`: Important business events
   - `DEBUG`: Detailed diagnostic info
   - `TRACE`: Very detailed (use sparingly)

2. **Include context**: Always log relevant IDs, usernames, etc.
   ```java
   logger.info("User {} logged in successfully", userId);
   ```

3. **Don't log sensitive data**: Passwords, tokens, credit cards, etc.

4. **Use parameterized logging**: More efficient than string concatenation
   ```java
   // Good
   logger.debug("Processing {} items", count);
   
   // Bad (string built even if DEBUG is disabled)
   logger.debug("Processing " + count + " items");
   ```

5. **Log exceptions properly**:
   ```java
   logger.error("Failed to process request", exception);
   ```
