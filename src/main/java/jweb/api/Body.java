package jweb.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The request body, parsed from JSON into the parameter's type — a record,
 * a POJO, a {@code Map} or a {@code List}; a {@code String} parameter gets
 * the raw text.
 *
 * <pre>{@code
 * @POST
 * public Map<String, Object> create(@Body User user) { ... }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Body {
    /** Whether an empty body is an error (400). */
    boolean required() default true;
}
