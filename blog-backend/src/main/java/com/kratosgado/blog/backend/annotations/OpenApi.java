package com.kratosgado.blog.backend.annotations;

import com.kratosgado.blog.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class OpenApi {

  /** Base Security & Documentation Annotation */
  @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public @interface SecuredEndpoint {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Protected endpoint";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "";

    UserRole[] roles() default {UserRole.ADMIN};
  }

  /** Base Success Response */
  @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Operation
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Success")})
  public @interface SuccessEndpoint {}

  /** Standard GET Response (Success + 404) */
  @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @SuccessEndpoint
  @ApiResponses({@ApiResponse(responseCode = "404", description = "Resource not found")})
  public @interface GetEndpoint {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Retrieve resource";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "";
  }

  @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @GetEndpoint
  @SecuredEndpoint
  public @interface SecuredGetEndpoint {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Retrieve resource (Secured)";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "";

    @AliasFor(annotation = SecuredEndpoint.class, attribute = "roles")
    UserRole[] roles() default {UserRole.ADMIN};
  }

  /** Standard Update Response (GET + 400 + 500) */
  @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @GetEndpoint
  @ApiResponses({
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public @interface UpdateEndpoint {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Update resource";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "";
  }

  // --- Combined Secured Leaf Annotations ---

  /** Secured Update: Combines Update Logic + Security */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @UpdateEndpoint
  @SecuredEndpoint
  public @interface SecuredUpdateEndpoint {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Update resource (Secured)";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "";

    @AliasFor(annotation = SecuredEndpoint.class, attribute = "roles")
    UserRole[] roles() default {UserRole.ADMIN};
  }

  /** Secured Create: Adds 201 Created + Security */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @UpdateEndpoint
  @SecuredEndpoint
  @ResponseStatus(HttpStatus.CREATED)
  @ApiResponses({@ApiResponse(responseCode = "201", description = "Created")})
  public @interface SecuredCreateEndpoint {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Create resource (Secured)";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "";

    @AliasFor(annotation = SecuredEndpoint.class, attribute = "roles")
    UserRole[] roles() default {UserRole.ADMIN};
  }

  /** Secured Delete: Adds 204 No Content + Security */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @GetEndpoint
  @SecuredEndpoint
  @ApiResponses({@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  public @interface DeleteEndpoint {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Delete resource (Secured)";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "";

    @AliasFor(annotation = SecuredEndpoint.class, attribute = "roles")
    UserRole[] roles() default {UserRole.ADMIN};
  }
}
