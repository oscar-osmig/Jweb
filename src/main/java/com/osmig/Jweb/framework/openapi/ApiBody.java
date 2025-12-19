package com.osmig.Jweb.framework.openapi;

import java.lang.annotation.*;

/**
 * Documents a request body.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiBody {

    /**
     * Description of the request body.
     */
    String description() default "";

    /**
     * Content type.
     */
    String contentType() default "application/json";

    /**
     * Whether the body is required.
     */
    boolean required() default true;

    /**
     * Example body content.
     */
    String example() default "";
}
