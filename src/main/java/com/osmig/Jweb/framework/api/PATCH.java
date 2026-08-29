package com.osmig.Jweb.framework.api;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP PATCH endpoint. Shorthand for @PatchMapping.
 *
 * @deprecated Replaced by {@code jweb.api.PATCH} — shorter import, same behavior. Existing code keeps working.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMethod.PATCH)
@Deprecated
public @interface PATCH {
    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] value() default {};
}
