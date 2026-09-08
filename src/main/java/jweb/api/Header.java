package jweb.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A request header of a {@code @REST} method:
 *
 * <pre>{@code
 * public Map<String, Object> info(@Header("User-Agent") String agent,
 *                                 @Header(value = "X-Trace", required = false) String trace) { ... }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Header {
    /** The header name; defaults to the parameter's name. */
    String value() default "";
    String defaultValue() default "";
    boolean required() default true;
}
