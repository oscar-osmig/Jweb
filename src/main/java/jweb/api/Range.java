package jweb.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bounds a numeric record component bound from request parameters
 * (see {@code app.action}); out of range is a 400 naming the component.
 *
 * <pre>{@code
 * record Vane(@Range(min = 1, max = 3) int n, Setting set) {}
 * }</pre>
 */
@Target({ElementType.RECORD_COMPONENT, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Range {
    double min() default Double.NEGATIVE_INFINITY;
    double max() default Double.POSITIVE_INFINITY;
}
