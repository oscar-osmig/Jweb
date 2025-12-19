package com.osmig.Jweb.framework.openapi;

import java.lang.annotation.*;

/**
 * Documents an API response.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiResponse {

    /**
     * HTTP status code.
     */
    int code();

    /**
     * Description of the response.
     */
    String description() default "";

    /**
     * Content type (e.g., "application/json").
     */
    String contentType() default "application/json";

    /**
     * Example response body.
     */
    String example() default "";
}
