package jweb.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A regular expression the whole value of a record component bound from
 * request parameters must match (see {@code app.action}); a mismatch is a
 * 400 with {@link #message()} or a generic one.
 *
 * <pre>{@code
 * record Slug(@Pattern("[a-z0-9-]+") String name) {}
 * }</pre>
 */
@Target({ElementType.RECORD_COMPONENT, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Pattern {
    String value();
    String message() default "";
}
