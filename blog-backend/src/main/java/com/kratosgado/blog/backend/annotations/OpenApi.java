package com.kratosgado.blog.backend.annotations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class OpenApi {

  @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Operation(summary = "Protected endpoint", security = @SecurityRequirement(name = "bearer-jwt"))
  @ApiResponses({
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public @interface SecuredEndpoint {}

  @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
  })
  public @interface SuccessEndpoint {}

  @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponses({@ApiResponse(responseCode = "404", description = "Resource not found")})
  @SuccessEndpoint
  public @interface GetEndpoint {}

  @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponses({
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetEndpoint
  public @interface UpdateEndpoint {}

  @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @UpdateEndpoint
  @SecuredEndpoint
  public @interface SecuredUpdateEndpoint {}

  @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created"),
  })
  @UpdateEndpoint
  public @interface CreateEndpoint {}

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @CreateEndpoint
  @SecuredEndpoint
  public @interface SecuredCreateEndpoint {}

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @GetEndpoint
  @SecuredEndpoint
  public @interface DeleteEndpoint {}
}
