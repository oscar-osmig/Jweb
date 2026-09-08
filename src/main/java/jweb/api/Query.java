package jweb.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A query-string (or form) parameter of a {@code @REST} method:
 *
 * <pre>{@code
 * @GET("/search")
 * public List<Item> search(@Query String q,
 *                          @Query(value = "limit", defaultValue = "10") int limit,
 *                          @Query Optional<String> sort) { ... }
 * }</pre>
 *
 * <p>Absent with no default: an {@code Optional} is empty, a {@code List}
 * is empty, anything else is a 400.</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {
    /** The parameter name; defaults to the parameter's name. */
    String value() default "";
    /** The value used when the parameter is absent. */
    String defaultValue() default "";
    /** Whether absence (with no default) is an error (400). */
    boolean required() default true;
}
