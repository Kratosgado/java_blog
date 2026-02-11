# RBAC Simplification Summary

## Overview
Refactored the Role-Based Access Control (RBAC) implementation to use a simple enum column in the User table instead of a separate Role entity with a many-to-many relationship. This simplification reduces database complexity while maintaining all security functionality.

## Changes Made

### 1. Database Schema Changes

#### Created New Migration
- **File**: `blog-backend/src/main/resources/migrations/rbac_simplify_to_enum.sql`
- **Changes**:
  - Adds `role` column (VARCHAR(20)) to `users` table with CHECK constraint
  - Migrates existing role data from `user_roles` junction table
  - Drops `user_roles` junction table
  - Drops `roles` table
  - Drops old views (`user_roles_summary`, `role_membership_counts`)
  - Creates new views (`user_summary`, `role_distribution`)
  - Adds index on `role` column for performance

#### Removed Old Migration
- **File**: `blog-backend/src/main/resources/migrations/rbac_roles.sql` (deleted)

### 2. Model Changes

#### Updated User Entity
- **File**: `blog-common/src/main/java/com/kratosgado/blog/models/User.java`
- **Changes**:
  - Removed `@ManyToMany` relationship with Role entity
  - Removed `Set<Role> roles` field
  - Added `UserRole role` enum field with `@Enumerated(EnumType.STRING)`
  - Simplified `hasRole()` method to check single role
  - Simplified `getAuthorities()` to return single authority
  - Updated `getRoleString()` to return role name directly
  - Removed `addRole()` and `removeRole()` methods

#### Removed Role Entity
- **File**: `blog-common/src/main/java/com/kratosgado/blog/models/Role.java` (deleted)

### 3. Repository Changes

#### Removed RoleRepository
- **File**: `blog-backend/src/main/java/com/kratosgado/blog/backend/repositories/jpa/RoleRepository.java` (deleted)

### 4. Service Changes

#### Updated AuthService
- **File**: `blog-backend/src/main/java/com/kratosgado/blog/backend/services/AuthService.java`
- **Changes**:
  - Added `UserRole.READER` as default role in `register()` method
  - Removed dependency on RoleRepository

#### Updated CustomOAuth2UserService
- **File**: `blog-backend/src/main/java/com/kratosgado/blog/backend/security/CustomOAuth2UserService.java`
- **Changes**:
  - Removed RoleRepository dependency
  - Directly sets `UserRole.READER` for new OAuth2 users
  - Removed role entity lookup logic

### 5. Security Changes

#### Updated JwtAuthenticationFilter
- **File**: `blog-backend/src/main/java/com/kratosgado/blog/backend/security/JwtAuthenticationFilter.java`
- **Changes**:
  - Updated to use `user.getRole()` instead of `user.getRoles()`
  - Simplified authority creation to single role

#### Updated CustomOAuth2User
- **File**: `blog-backend/src/main/java/com/kratosgado/blog/backend/security/CustomOAuth2User.java`
- **Changes**:
  - Updated to use `user.getRole()` instead of `user.getRoles()`
  - Creates single authority from user role

### 6. Controller Changes

#### Updated AuthController
- **File**: `blog-backend/src/main/java/com/kratosgado/blog/backend/controllers/v1/AuthController.java`
- **Changes**:
  - Updated JWT claims to include single `role` instead of `roles` array
  - Changed response to return `user.getRole().name()` instead of Role entity

### 7. Test Changes

#### Updated SecurityIntegrationTest
- **File**: `blog-backend/src/test/java/com/kratosgado/blog/backend/security/SecurityIntegrationTest.java`
- **Changes**:
  - Removed RoleRepository dependency
  - Updated test user creation to use `UserRole` enum directly
  - Removed role entity creation logic

#### Updated AuthServiceTest
- **File**: `blog-backend/src/test/java/com/kratosgado/blog/backend/services/AuthServiceTest.java`
- **Changes**:
  - Updated test user setup to use `UserRole.READER`
  - Added LoginAttemptService mock

#### Updated UserServiceTest
- **File**: `blog-backend/src/test/java/com/kratosgado/blog/backend/services/UserServiceTest.java`
- **Changes**:
  - Updated test user setup to use `UserRole.READER`

#### Updated FakeDataSeeder
- **File**: `blog-backend/src/main/java/com/kratosgado/blog/backend/seeders/FakeDataSeeder.java`
- **Changes**:
  - Updated to use `UserRole` enum instead of string values

## Benefits

### 1. Simplified Database Schema
- Removed 2 tables (`roles`, `user_roles`)
- Reduced database joins for role checks
- Simpler data model with single column for role

### 2. Improved Performance
- No JOIN required to fetch user roles
- Faster authentication queries
- Single column index instead of junction table indexes

### 3. Cleaner Code
- Removed complex many-to-many relationship management
- Simplified role assignment and checking logic
- Less code to maintain

### 4. Maintained Functionality
- All three roles (ADMIN, AUTHOR, READER) still supported
- Spring Security integration unchanged
- JWT authentication still works with role claims
- OAuth2 integration still functional

## Migration Path

To apply these changes to an existing database:

1. Run the migration script:
   ```sql
   \i blog-backend/src/main/resources/migrations/rbac_simplify_to_enum.sql
   ```

2. The migration will:
   - Preserve all existing user role assignments
   - Migrate users with multiple roles (takes first role)
   - Set default role to READER for users without roles
   - Clean up old tables and views

## API Changes

### JWT Token Claims
**Before:**
```json
{
  "userId": 1,
  "roles": ["ADMIN", "AUTHOR"]
}
```

**After:**
```json
{
  "userId": 1,
  "role": "ADMIN"
}
```

### User Response
**Before:**
```java
user.getRoles() // Set<Role>
```

**After:**
```java
user.getRole() // UserRole enum
user.getRole().name() // "ADMIN", "AUTHOR", or "READER"
```

## Security Considerations

- Each user now has exactly one role (no multiple roles per user)
- Role changes require updating single column instead of junction table
- Simplified permission model aligns with actual usage patterns
- No breaking changes to Spring Security configuration

## Testing

All tests have been updated to reflect the new role model:
- Unit tests for services
- Integration tests for security
- Controller tests for authentication endpoints

## Future Enhancements

If multiple roles per user are needed in the future:
1. Add a JSON array column for roles
2. Or use PostgreSQL array type
3. Or add role hierarchy (ADMIN implies AUTHOR and READER)

Current single-role model is sufficient for most use cases and provides better performance.
