package jweb.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A cookie value of a {@code @REST} method:
 *
 * <pre>{@code
 * public Map<String, Object> me(@Cookie("session") String session) { ... }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cookie {
    /** The cookie name; defaults to the parameter's name. */
    String value() default "";
    String defaultValue() default "";
    boolean required() default true;
}
