package com.osmig.Jweb.framework.openapi;

import java.lang.annotation.*;

/**
 * Documents an API parameter.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParam {

    /**
     * Parameter name.
     */
    String name();

    /**
     * Where the parameter is located.
     */
    In in() default In.QUERY;

    /**
     * Description of the parameter.
     */
    String description() default "";

    /**
     * Whether the parameter is required.
     */
    boolean required() default false;

    /**
     * Data type (string, integer, boolean, etc.).
     */
    String type() default "string";

    /**
     * Example value.
     */
    String example() default "";

    /**
     * Parameter location.
     */
    enum In {
        PATH, QUERY, HEADER, COOKIE
    }
}
