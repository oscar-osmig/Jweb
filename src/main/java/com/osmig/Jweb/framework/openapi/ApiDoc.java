package com.osmig.Jweb.framework.openapi;

import java.lang.annotation.*;

/**
 * Documents an API endpoint for OpenAPI/Swagger generation.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * @ApiDoc(
 *     summary = "Get user by ID",
 *     description = "Returns a user by their unique identifier",
 *     tags = {"users"},
 *     responses = {
 *         @ApiResponse(code = 200, description = "User found"),
 *         @ApiResponse(code = 404, description = "User not found")
 *     }
 * )
 * public Element getUser(Request req) {
 *     // ...
 * }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiDoc {

    /**
     * Short summary of the operation.
     */
    String summary() default "";

    /**
     * Detailed description.
     */
    String description() default "";

    /**
     * Tags for grouping operations.
     */
    String[] tags() default {};

    /**
     * Whether the endpoint is deprecated.
     */
    boolean deprecated() default false;

    /**
     * Response definitions.
     */
    ApiResponse[] responses() default {};

    /**
     * Parameter definitions.
     */
    ApiParam[] params() default {};

    /**
     * Request body definition.
     */
    ApiBody body() default @ApiBody;
}
