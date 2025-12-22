package com.osmig.Jweb.framework.api;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a REST API controller.
 * Shorthand for @RestController + @RequestMapping.
 *
 * <pre>{@code
 * @REST("/api/users")
 * public class UserApi {
 *     @GET
 *     public List<User> all() { }
 *
 *     @GET("/{id}")
 *     public User get(@PathVariable Long id) { }
 *
 *     @POST
 *     public User create(@RequestBody User user) { }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@RestController
@RequestMapping
public @interface REST {
    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] value() default {};
}
