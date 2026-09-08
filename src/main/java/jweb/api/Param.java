package jweb.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A path parameter of a {@code @REST} method, converted to the parameter's
 * type (String, numbers, boolean, UUID, enums):
 *
 * <pre>{@code
 * @GET("/{id}")
 * public User get(@Param int id) { ... }          // name = the parameter's name
 * @GET("/{userId}/posts/{slug}")
 * public Post post(@Param("userId") long user, @Param String slug) { ... }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    /** The placeholder name; defaults to the parameter's name. */
    String value() default "";
}
